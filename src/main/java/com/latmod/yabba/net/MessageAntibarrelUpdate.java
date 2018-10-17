package com.latmod.yabba.net;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.item.ItemEntryWithCount;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.latmod.yabba.gui.GuiAntibarrel;
import com.latmod.yabba.tile.TileAntibarrel;
import com.latmod.yabba.util.AntibarrelData;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageAntibarrelUpdate extends MessageToClient {
  private BlockPos pos;
  private ByteBuf buffer;
  private boolean reset;

  public static void send(TileAntibarrel antibarrel, EntityPlayerMP player) {

    ByteBuf buffer = Unpooled.buffer();
    ByteBuf b = Unpooled.buffer();
    boolean reset = true;
    for (ItemEntryWithCount entry : antibarrel.contents.items.values()) {
      if (!entry.isEmpty()) {
        ByteBufUtils.writeTag(b, entry.serializeNBT());
        if (buffer.writerIndex() + b.writerIndex() > 30000) {
          new MessageAntibarrelUpdate(antibarrel, buffer, reset).sendTo(player);
          buffer = Unpooled.buffer();
          reset = false;
        }
        buffer.writeBytes(b);
        b.readerIndex(0);
        b.writerIndex(0);
      }
    }
    b.release();

    if (buffer.writerIndex() > 0) {
      new MessageAntibarrelUpdate(antibarrel, buffer, reset).sendTo(player);
    } else {
      buffer.release();
    }
  }

  public MessageAntibarrelUpdate() {
  }

  public MessageAntibarrelUpdate(TileAntibarrel antibarrel, ByteBuf buffer, boolean reset) {
    pos = antibarrel.getPos();
    this.buffer = buffer;
    this.reset = reset;
  }

  @Override
  public NetworkWrapper getWrapper() {
    return YabbaNetHandler.NET;
  }

  @Override
  public void writeData(DataOut data) {
    data.writePos(pos);
    data.writeBoolean(reset);
    byte[] bytes = new byte[buffer.readableBytes()];
    buffer.readBytes(bytes);
    data.writeInt(bytes.length);
    data.writeBytes(bytes);
    buffer.release();
  }

  @Override
  public void readData(DataIn data) {
    pos = data.readPos();
    reset = data.readBoolean();
    int len = data.readInt();
    byte[] bytes = new byte[len];
    data.readBytes(bytes);
    buffer = Unpooled.copiedBuffer(bytes);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void onMessage() {
    TileEntity tileEntity = ClientUtils.MC.world.getTileEntity(pos);

    if (tileEntity instanceof TileAntibarrel) {
      AntibarrelData contents = ((TileAntibarrel) tileEntity).contents;

      if (reset) {
        contents.clear();
      }
      while (buffer.isReadable()) {
        ItemEntryWithCount entryc = new ItemEntryWithCount(ByteBufUtils.readTag(buffer));
        if (!entryc.isEmpty()) {
          contents.items.put(entryc.entry, entryc);
        }
      }

      GuiAntibarrel gui = ClientUtils.getCurrentGuiAs(GuiAntibarrel.class);

      if (gui != null) {
        gui.refreshWidgets();
      }
    }
    buffer.release();
  }
}