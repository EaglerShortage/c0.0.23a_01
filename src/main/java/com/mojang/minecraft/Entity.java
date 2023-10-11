package com.mojang.minecraft;

import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.level.liquid.Liquid;
import com.mojang.minecraft.level.tile.Tile;
import com.mojang.minecraft.phys.AABB;
import com.mojang.minecraft.renderer.Textures;
import java.io.Serializable;
import java.util.ArrayList;

public class Entity implements Serializable {
	public static final long serialVersionUID = 0L;
	protected Level level;
	public float xo;
	public float yo;
	public float zo;
	public float x;
	public float y;
	public float z;
	public float xd;
	public float yd;
	public float zd;
	public float yRot;
	public float xRot;
	public float yRotO;
	public float xRotO;
	public AABB bb;
	public boolean onGround = false;
	public boolean horizontalCollision = false;
	public boolean removed = false;
	public float heightOffset = 0.0F;
	protected float bbWidth = 0.6F;
	public float bbHeight = 1.8F;
	private float walkDist = 0.0F;
	public boolean makeStepSound = true;

	public Entity(Level var1) {
		this.level = var1;
		this.setPos(0.0F, 0.0F, 0.0F);
	}

	protected void resetPos() {
		if(this.level != null) {
			float var1 = (float)this.level.xSpawn + 0.5F;
			float var2 = (float)this.level.ySpawn;

			for(float var3 = (float)this.level.zSpawn + 0.5F; var2 > 0.0F; ++var2) {
				this.setPos(var1, var2, var3);
				if(this.level.getCubes(this.bb).size() == 0) {
					break;
				}
			}

			this.xd = this.yd = this.zd = 0.0F;
			this.yRot = this.level.rotSpawn;
			this.xRot = 0.0F;
		}
	}

	public void remove() {
		this.removed = true;
	}

	public void setSize(float var1, float var2) {
		this.bbWidth = var1;
		this.bbHeight = var2;
	}

	protected void setRot(float var1, float var2) {
		this.yRot = var1;
		this.xRot = var2;
	}

	public void setPos(float var1, float var2, float var3) {
		this.x = var1;
		this.y = var2;
		this.z = var3;
		float var4 = this.bbWidth / 2.0F;
		float var5 = this.bbHeight / 2.0F;
		this.bb = new AABB(var1 - var4, var2 - var5, var3 - var4, var1 + var4, var2 + var5, var3 + var4);
	}

	public void turn(float var1, float var2) {
		float var3 = this.xRot;
		float var4 = this.yRot;
		this.yRot = (float)((double)this.yRot + (double)var1 * 0.15D);
		this.xRot = (float)((double)this.xRot - (double)var2 * 0.15D);
		if(this.xRot < -90.0F) {
			this.xRot = -90.0F;
		}

		if(this.xRot > 90.0F) {
			this.xRot = 90.0F;
		}

		this.xRotO += this.xRot - var3;
		this.yRotO += this.yRot - var4;
	}

	public void interpolateTurn(float var1, float var2) {
		this.yRot = (float)((double)this.yRot + (double)var1 * 0.15D);
		this.xRot = (float)((double)this.xRot - (double)var2 * 0.15D);
		if(this.xRot < -90.0F) {
			this.xRot = -90.0F;
		}

		if(this.xRot > 90.0F) {
			this.xRot = 90.0F;
		}

	}

	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		this.xRotO = this.xRot;
		this.yRotO = this.yRot;
	}

	public boolean isFree(float var1, float var2, float var3) {
		AABB var4 = this.bb.cloneMove(var1, var2, var3);
		ArrayList var5 = this.level.getCubes(var4);
		return var5.size() > 0 ? false : !this.level.containsAnyLiquid(var4);
	}

	public void move(float var1, float var2, float var3) {
		float var4 = this.x;
		float var5 = this.z;
		float var6 = var1;
		float var7 = var2;
		float var8 = var3;
		ArrayList var9 = this.level.getCubes(this.bb.expand(var1, var2, var3));

		int var10;
		for(var10 = 0; var10 < var9.size(); ++var10) {
			var2 = ((AABB)var9.get(var10)).clipYCollide(this.bb, var2);
		}

		this.bb.move(0.0F, var2, 0.0F);

		for(var10 = 0; var10 < var9.size(); ++var10) {
			var1 = ((AABB)var9.get(var10)).clipXCollide(this.bb, var1);
		}

		this.bb.move(var1, 0.0F, 0.0F);

		for(var10 = 0; var10 < var9.size(); ++var10) {
			var3 = ((AABB)var9.get(var10)).clipZCollide(this.bb, var3);
		}

		this.bb.move(0.0F, 0.0F, var3);
		this.horizontalCollision = var6 != var1 || var8 != var3;
		this.onGround = var7 != var2 && var7 < 0.0F;
		if(var6 != var1) {
			this.xd = 0.0F;
		}

		if(var7 != var2) {
			this.yd = 0.0F;
		}

		if(var8 != var3) {
			this.zd = 0.0F;
		}

		this.x = (this.bb.x0 + this.bb.x1) / 2.0F;
		this.y = this.bb.y0 + this.heightOffset;
		this.z = (this.bb.z0 + this.bb.z1) / 2.0F;
		float var13 = this.x - var4;
		var1 = this.z - var5;
		this.walkDist = (float)((double)this.walkDist + Math.sqrt((double)(var13 * var13 + var1 * var1)) * 0.6D);
		if(this.makeStepSound) {
			int var11 = this.level.getTile((int)this.x, (int)(this.y - 0.2F - this.heightOffset), (int)this.z);
			if(this.walkDist > 1.0F && var11 > 0) {
				Tile.SoundType var12 = Tile.tiles[var11].soundType;
				if(var12 != Tile.SoundType.none) {
					this.walkDist -= (float)((int)this.walkDist);
					//this.playSound("step." + var12.name, var12.getVolume() * (12.0F / 16.0F), var12.getPitch());
				}
			}
		}

	}

	public boolean isInWater() {
		return this.level.containsLiquid(this.bb.grow(0.0F, -0.4F, 0.0F), Liquid.water);
	}

	public boolean isInLava() {
		return this.level.containsLiquid(this.bb.grow(0.0F, -0.4F, 0.0F), Liquid.lava);
	}

	public void moveRelative(float var1, float var2, float var3) {
		float var4 = (float)Math.sqrt((double)(var1 * var1 + var2 * var2));
		if(var4 >= 0.01F) {
			if(var4 < 1.0F) {
				var4 = 1.0F;
			}

			var4 = var3 / var4;
			var1 *= var4;
			var2 *= var4;
			var3 = (float)Math.sin((double)this.yRot * Math.PI / 180.0D);
			var4 = (float)Math.cos((double)this.yRot * Math.PI / 180.0D);
			this.xd += var1 * var4 - var2 * var3;
			this.zd += var2 * var4 + var1 * var3;
		}
	}

	public boolean isLit() {
		int var1 = (int)this.x;
		int var2 = (int)this.y;
		int var3 = (int)this.z;
		return this.level.isLit(var1, var2, var3);
	}

	public float getBrightness() {
		int var1 = (int)this.x;
		int var2 = (int)(this.y + this.heightOffset / 2.0F);
		int var3 = (int)this.z;
		return this.level.getBrightness(var1, var2, var3);
	}

	public void render(Textures var1, float var2) {
	}

	public void setLevel(Level var1) {
		this.level = var1;
	}

	public void moveTo(float var1, float var2, float var3, float var4, float var5) {
		this.xo = this.x = var1;
		this.yo = this.y = var2;
		this.zo = this.z = var3;
		this.yRot = var4;
		this.xRot = var5;
		this.setPos(var1, var2, var3);
	}

	public float distanceTo(Entity var1) {
		float var2 = this.x - var1.x;
		float var3 = this.y - var1.y;
		float var4 = this.z - var1.z;
		return (float)Math.sqrt((double)(var2 * var2 + var3 * var3 + var4 * var4));
	}
}
