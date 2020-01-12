package alexiil.mc.mod.load.render;

import alexiil.mc.mod.load.baked.BakedConfig;
import cn.minerealms.minecraft.client;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class TextureAnimator {
  private static final int TEXTURE_MILLI_SECONDS = 16384;
  private static final int TEXTURE_PIXEL_CAP = 39321600;
  private static final int TEXTURE_PIXEL_MIN = 204800;
  private static final int TEXTURE_UPLOAD_AHEAD = 10;
  
  public class AnimatedTexture {
    private final int[] ids;
    private final long[] lastUsed;
    
    public AnimatedTexture(BufferedImage[] images, int totalPixels) {
      this.images = images;
      this.ids = new int[images.length];
      this.lastUsed = new long[images.length];
      Arrays.fill(this.ids, -1);
      if (totalPixels <= 204800)
      
      { this.textureMilliSeconds = Integer.MAX_VALUE;
        for (int i = 0; i < images.length; i++)
          bindFrame(i);  }
      else if (totalPixels <= 39321600) { this.textureMilliSeconds = 16384; }
      else
      
      { int higherPower = MathHelper.func_151239_c(totalPixels / 39321600);
        this.textureMilliSeconds = 16384 >> higherPower; }
    
    }
    private final BufferedImage[] images; private final int textureMilliSeconds;
    public void uploadFramesAhead(int frame, int number) {
      for (int f = frame + 1; f < frame + number; f++) {
        int wf = (f >= this.images.length) ? (f - this.images.length) : f;
        if (wf >= this.images.length)
          break;  if (this.ids[wf] == -1) {
          this.ids[wf] = TextureUtil.func_110996_a();
          TextureUtil.func_110987_a(this.ids[wf], this.images[wf]);
        } 
      } 
    }
    
    public void bindFrame(int frame) {
      if (this.ids[frame] != -1) {
        GlStateManager.func_179144_i(this.ids[frame]);
      } else {
        this.ids[frame] = TextureUtil.func_110996_a();
        TextureUtil.func_110987_a(this.ids[frame], this.images[frame]);
        GlStateManager.func_179144_i(this.ids[frame]);
      } 
      this.lastUsed[frame] = TextureAnimator.this.now;
      uploadFramesAhead(frame, 10);
    }
    
    public void delete() {
      for (int i = 0; i < this.ids.length; i++) {
        if (this.ids[i] != -1) deleteFrame(i);
      
      } 
    }
    
    private void deleteFrame(int frame) {
      TextureUtil.func_147942_a(this.ids[frame]);
      this.ids[frame] = -1;
    }
    
    private void tick() {
      for (int i = 0; i < this.ids.length; i++) {
        if (this.ids[i] != -1 && 
          this.lastUsed[i] + this.textureMilliSeconds < TextureAnimator.this.now) {
          deleteFrame(i);
        }
      } 
    }
  }













  
  private Map<String, AnimatedTexture> animatedTextures = new HashMap<>();
  private long now = System.currentTimeMillis();
  
  public static boolean isAnimated(String resourceLocation) {
    ResourceLocation location = new ResourceLocation(resourceLocation);
    try {
      IResource res = Minecraft.func_71410_x().func_110442_L().func_110536_a(location);
      InputStream stream = res.func_110527_b();
      for (UnmodifiableIterator<ImageReader> unmodifiableIterator = ImmutableList.copyOf(ImageIO.getImageReaders(stream)).iterator(); unmodifiableIterator.hasNext(); ) { ImageReader reader = unmodifiableIterator.next();
        
        try { reader.setInput(stream);
          boolean animated = (reader.getNumImages(true) > 1);
          reader.dispose();
          return animated; }
        catch (IOException iOException) {  }
        finally { reader.dispose(); }
         }
    
    } catch (IOException iOException) {}
    return false;
  }
  
  public TextureAnimator(BakedConfig images) {
    Minecraft mc = Minecraft.func_71410_x();
    for (BakedRenderingPart render : images.renderingParts) {
      String resource = render.render.getLocation();
      if (resource != null && isAnimated(resource)) {
        try {
          IResource res = mc.func_110442_L().func_110536_a(new ResourceLocation(resource));
          InputStream stream = res.func_110527_b();
          BufferedImage[] frames = null;
          for (UnmodifiableIterator<ImageReader> unmodifiableIterator = ImmutableList.copyOf(ImageIO.getImageReaders(stream)).iterator(); unmodifiableIterator.hasNext(); ) { ImageReader reader = unmodifiableIterator.next();
            try {
              reader.setInput(stream);
              int size = 0;
              frames = new BufferedImage[reader.getNumImages(true)];
              for (int i = 0; i < frames.length; i++) {
                frames[i] = reader.read(i);
                size += frames[i].getHeight() * frames[i].getWidth();
              } 
              this.animatedTextures.put(resource, new AnimatedTexture(frames, size));
              reader.dispose();
              break;
            } catch (IOException iOException) {
            
            } finally {
              reader.dispose();
            }  }
        
        } catch (IOException iOException) {}
      }
    } 
  }


  
  public void tick() {
    this.now = System.currentTimeMillis();
    for (AnimatedTexture tex : this.animatedTextures.values()) {
      tex.tick();
    }
  }
  
  public void close() {
    for (AnimatedTexture tex : this.animatedTextures.values()) {
      tex.delete();
    }
  }
  
  public void bindTexture(String resource, int frame) {
    if (this.animatedTextures.containsKey(resource))
      ((AnimatedTexture)this.animatedTextures.get(resource)).bindFrame(frame); 
  }
}
