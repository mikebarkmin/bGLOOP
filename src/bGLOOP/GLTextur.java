package bGLOOP;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

/** Ein Texturobjekt, welches an ein {@link GLObjekt} geknüpft werden kann.
 * 
 * @author R. Spillner
 */
// the class acts as a proxy to the real texture implementations
public class GLTextur {

	private static HashMap<File, GLTextureImpl> textures = new HashMap<File, GLTextureImpl>(10);
	// the NULL_TEXTURE simplifies rendering, because we don't have to
	// distinguish between the cases "texture" and "no texture".
	// It's not meant to be exported to the user
	final static GLTextur NULL_TEXTURE = new GLTextur();
	GLTextureImpl aTexturImpl = null;

	// So why this? GLTextur objects are all mapped to
	// the same GLTextureImpl object if the texture file is the same.
	// This safes time and space when loading the textures and enables
	// faster drawing of textured objects
	static class GLTextureImpl {
		final static GLTextureImpl NULL_TEXTURE_IMPL = new GLTextureImpl(null);
		File aTexFile;
		Texture aTexture;
		private boolean aReady = false, cannotBeLoaded = false;

		public GLTextureImpl(File pFilename) {
			aTexFile = pFilename;
		}

		void load(GL2 gl) {
			if (aTexture == null && !cannotBeLoaded && !aReady && aTexFile != null)
				try {
					aTexture = TextureIO.newTexture(aTexFile, false);
					aTexture.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
					aTexture.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
					aReady = true;
				} catch (IOException e) {
					cannotBeLoaded = true;
					aReady = false;
					System.err.println("Die Datei " + aTexFile.getName() + " konnte nicht "
							+ "korrekt geladen werden. Prüfen Sie die weiteren " + "Fehlermeldungen.");
					e.printStackTrace();
				} catch (GLException gle) {
					gle.printStackTrace();
				}
		}

		void loadAndEnable(GL2 gl) {
			load(gl);
			if(aReady) {
				aTexture.enable(gl);
				aTexture.bind(gl);
			}
		}

		Texture getTexture() {
			return aTexture;
		}

		boolean isReady() {
			return aReady;
		}

		void disable(GL2 gl) {
			aTexture.disable(gl);
		}
	}

	private GLTextur() {
		aTexturImpl = GLTextureImpl.NULL_TEXTURE_IMPL;
	}

	/** Erstellt ein Textur-Objekt. Der Dateiname wird ausgehend von dem Pfad
	 * gesucht, von dem aus Java gestartet wurde.
	 * 
	 * @param pTexturDateiname Dateiname der Texturdatei.
	 */
	public GLTextur(String pTexturDateiname) {
			this(new File(pTexturDateiname));
	}

	GLTextur(File pFile) {
		File f = null;
		try {
			f = pFile.getCanonicalFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (f != null && (aTexturImpl = textures.get(f)) == null)
			textures.put(f, aTexturImpl = new GLTextureImpl(f));
	}

	boolean isReady() {
		return aTexturImpl.aReady;
	}

	boolean isLoadable() {
		return !aTexturImpl.cannotBeLoaded;
	}

	/*
	 * Load texture if not loaded before.
	 */
	void load(GL2 gl) {
		aTexturImpl.load(gl);
	}

	void loadAndEnable(GL2 gl) {
		aTexturImpl.loadAndEnable(gl);
	}

	void disable(GL2 gl) {
		aTexturImpl.disable(gl);
	}

	@Override
	public boolean equals(Object t) {
		return (t != null) && aTexturImpl == ((GLTextur)t).aTexturImpl;
	}

	GLTextur getObjectOrNull() {
		return this == NULL_TEXTURE ? null : this;
	}
}
