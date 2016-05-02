package ajb.core;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Vector;

import org.lwjgl.opengl.GL11;

public class RenderUtils {

	public static boolean createDefaultTexturesForPrimitives = true;
	public static boolean renderNormals = false;
	public static boolean smoothTextures = true;
	public static boolean useVertexNormals = true;
	public static float normalLength = 1.0f;

	private static final float roundOffCorrection = 100000;

	public static void renderPolyheadron(float[][][] polygons, float[][][] textureCords) {
		float[][] polygonSurfaceNormals = new float[polygons.length][];
		Vector<String> uniqueVertexHashes = new Vector<String>();
		Vector<float[]> uniqueVertexNormals = new Vector<float[]>();
		Vector<Vector<Integer>> polygonsWithUniqueVertex = new Vector<Vector<Integer>>();

		for (int n = 0; n < polygons.length; n++) {
			// calculate all the surface normals
			polygonSurfaceNormals[n] = VectorMath.crossProduct(VectorMath.subtractVectors(polygons[n][0], polygons[n][1]), VectorMath.subtractVectors(polygons[n][0], polygons[n][2]));

			// find all the unique verticies and record which polygons have
			// which
			for (int m = 0; m < polygons[n].length; m++) {
				String hash = VectorMath.hashVector3D(polygons[n][m]);
				int index = uniqueVertexHashes.indexOf(hash);
				if (index == -1) {
					uniqueVertexHashes.add(hash);
					Vector<Integer> polysWithVert = new Vector<Integer>();
					polysWithVert.add(new Integer(n));
					polygonsWithUniqueVertex.add(polysWithVert);
				} else {
					polygonsWithUniqueVertex.get(index).add(new Integer(n));
				}
			}
		}

		// what sides should we smooth
		for (int n = 0; n < uniqueVertexHashes.size(); n++) {
			boolean smooth = true;
			Vector<Integer> polysWithVert=null;
			if (useVertexNormals) {
				polysWithVert = polygonsWithUniqueVertex.get(n);

				for (int m = 0; m < polysWithVert.size(); m++) {
					float[] sideANormal = polygonSurfaceNormals[polysWithVert.get(m)];
					for (int k = m + 1; k < polysWithVert.size(); k++) {
						float[] sideBNormal = polygonSurfaceNormals[polysWithVert.get(k)];

						// sides must be between a certain angle of each other
						float cutOffAngle = 91f / 180f * (float) Math.PI;
						if (VectorMath.angleBetween(sideANormal, sideBNormal) > cutOffAngle) {
							smooth = false;
							m = polysWithVert.size();
							break;
						}
					}
				}
			} else {
				smooth = false;
			}
			if (smooth) {
				// average the normals
				float[] sumOfNormals = new float[] { 0, 0, 0 };
				for (int m = 0; m < polysWithVert.size(); m++) {
					sumOfNormals = VectorMath.addVectors(sumOfNormals, polygonSurfaceNormals[polysWithVert.get(m)]);
				}
				uniqueVertexNormals.add(VectorMath.normalize(sumOfNormals));
			} else {
				// no single vertex normal exists at this point
				uniqueVertexNormals.add(null);
			}

		}

		Vector<float[]> normalLines = null;
		if (renderNormals) {
			normalLines = new Vector<float[]>();
		}

		// render
		for (int n = 0; n < polygons.length; n++) {

			if (polygons[n].length == 3) {
				GL11.glBegin(GL11.GL_TRIANGLES);
			} else if (polygons[n].length == 4) {
				GL11.glBegin(GL11.GL_QUADS);
			} else {
				GL11.glBegin(GL11.GL_POLYGON);
			}

			for (int m = 0; m < polygons[n].length; m++) {

				float[] vertexNormal = uniqueVertexNormals.get(uniqueVertexHashes.indexOf(VectorMath.hashVector3D(polygons[n][m])));
				float[] normal;
				if (vertexNormal == null || (!useVertexNormals)) {
					// this edge is too damn sharp use the surface normal
					normal = VectorMath.cloneVector(polygonSurfaceNormals[n]);
				} else {
					normal = VectorMath.cloneVector(vertexNormal);
				}
				GL11.glNormal3f(normal[0], normal[1], normal[2]);

				// texture
				if (textureCords != null) {
					GL11.glTexCoord2f(textureCords[n][m][0], textureCords[n][m][1]);
				}

				// vertecies
				GL11.glVertex3f(polygons[n][m][0], polygons[n][m][1], polygons[n][m][2]);

				// also lets render some normals
				if (renderNormals) {
					normalLines.add(polygons[n][m]);
					normalLines.add(normal);
				}

			}
			GL11.glEnd();
		}

		if (renderNormals) {
			GL11.glBegin(GL11.GL_LINES);
			for (int n = 0; n < normalLines.size(); n += 2) {
				float[] cord = normalLines.get(n);
				float[] norm = normalLines.get(n + 1);
				float[] end = VectorMath.addVectors(norm, cord);
				GL11.glNormal3f(norm[0], norm[1], norm[2]);
				GL11.glVertex3f(cord[0], cord[1], cord[2]);
				GL11.glVertex3f(end[0], end[1], end[2]);
			}
			GL11.glEnd();
		}
	}

	public static void extrudePolygon(float[][] verticies, float height, float[][][] textureCords) {
		float[][][] texCords = null;
		if (textureCords == null) {
			texCords = new float[2 + verticies.length][][];
		}
		float high = height / 2;
		float[][][] polyhead = new float[2 + verticies.length][][];
		// ends

		float[][] textureFace = null;
		if (textureCords == null && createDefaultTexturesForPrimitives) {
			textureFace = new float[verticies.length][];
			float[] midPoint = new float[] { 0, 0 };
			for (int n = 0; n < textureFace.length; n++) {
				midPoint = VectorMath.addVectors(midPoint, verticies[n]);
			}
			float maxDis = Float.MIN_VALUE;
			for (int n = 0; n < textureFace.length; n++) {
				float dis = VectorMath.distance(midPoint, verticies[n]);
				if (dis > maxDis) {
					maxDis = dis;
				}
			}
			for (int n = 0; n < textureFace.length; n++) {
				textureFace[n] = VectorMath.divideVectors(VectorMath.addVectors(VectorMath.divideVectors(VectorMath.subtractVectors(verticies[n], midPoint), maxDis), new float[] { 1, 1 }), 2f);
			}
		}
		for (int y = 0; y < 2; y++) {
			float[][] face = new float[verticies.length][];
			for (int n = 0; n < face.length; n++) {
				face[n] = new float[] { verticies[n][0], (y * -1f + 1f) * high, verticies[n][1] };
			}
			polyhead[y] = face;
			if (textureCords == null && createDefaultTexturesForPrimitives) {
				texCords[y] = textureFace;
			}
		}
		// sides
		for (int n = 0; n < verticies.length; n++) {
			int m = (n + 1) % verticies.length;
			polyhead[n + 2] = new float[][] { new float[] { verticies[n][0], -high, verticies[n][1] }, new float[] { verticies[n][0], high, verticies[n][1] },
					new float[] { verticies[m][0], high, verticies[m][1] }, new float[] { verticies[m][0], -high, verticies[m][1] } };
			if (textureCords == null && createDefaultTexturesForPrimitives) {
				texCords[n + 2] = new float[][] { new float[] { 0, 0 }, new float[] { 0, 1 }, new float[] { 1, 1 }, new float[] { 1, 0 } };
			}
		}
		if (createDefaultTexturesForPrimitives && textureCords == null) {
			textureCords = texCords;
		}

		renderPolyheadron(polyhead, textureCords);
	}

	public static void createCube(float size, float[][][] textureCords) {
		createBox(size, size, size, textureCords);
	}

	public static void createBox(float wide, float high, float len, float[][][] textureCords) {
		float width = wide / 2f;
		float height = high / 2f;
		float length = len / 2f;
		float[][][] verts = new float[][][] {
				// back
				new float[][] { new float[] { -width, -height, -length }, new float[] { -width, height, -length }, new float[] { width, height, -length }, new float[] { width, -height, -length } },
				// front
				new float[][] { new float[] { -width, -height, length }, new float[] { width, -height, length }, new float[] { width, height, length }, new float[] { -width, height, length } },
				// left
				new float[][] { new float[] { -width, -height, -length }, new float[] { -width, -height, length }, new float[] { -width, height, length }, new float[] { -width, height, -length } },
				// right
				new float[][] { new float[] { width, -height, -length }, new float[] { width, height, -length }, new float[] { width, height, length }, new float[] { width, -height, length } },
				// bottom
				new float[][] { new float[] { -width, -height, -length }, new float[] { width, -height, -length }, new float[] { width, -height, length }, new float[] { -width, -height, length } },
				// top
				new float[][] { new float[] { -width, height, -length }, new float[] { -width, height, length }, new float[] { width, height, length }, new float[] { width, height, -length } } };
		if (textureCords == null && createDefaultTexturesForPrimitives) {
			// back
			textureCords = new float[][][] { new float[][] { new float[] { 1, 1 }, new float[] { 1, 0 }, new float[] { 0, 0 }, new float[] { 0, 1 } },
					// front
					new float[][] { new float[] { 0, 1 }, new float[] { 1, 1 }, new float[] { 1, 0 }, new float[] { 0, 0 } },
					// left
					new float[][] { new float[] { 0, 1 }, new float[] { 1, 1 }, new float[] { 1, 0 }, new float[] { 0, 0 } },
					// right
					new float[][] { new float[] { 1, 1 }, new float[] { 1, 0 }, new float[] { 0, 0 }, new float[] { 0, 1 } },
					// bottom
					new float[][] { new float[] { 0, 1 }, new float[] { 1, 1 }, new float[] { 1, 0 }, new float[] { 0, 0 } },
					// top
					new float[][] { new float[] { 0, 0 }, new float[] { 0, 1 }, new float[] { 1, 1 }, new float[] { 1, 0 } } };
		}

		/*
		 * float normalCompMag=(float)Math.pow(3f, 1f/3f);
		 * GL11.glBegin(GL11.GL_QUADS); //front GL11.glNormal3f(-normalCompMag,
		 * -normalCompMag, -normalCompMag); GL11.glVertex3f(-width, -height,
		 * -length); GL11.glNormal3f(-normalCompMag, normalCompMag,
		 * -normalCompMag); GL11.glVertex3f(-width, height, -length);
		 * GL11.glNormal3f(normalCompMag, normalCompMag, -normalCompMag);
		 * GL11.glVertex3f(width, height, -length);
		 * GL11.glNormal3f(normalCompMag, -normalCompMag, -normalCompMag);
		 * GL11.glVertex3f(width, -height, -length); //back GL11.glEnd();
		 */
		if (createDefaultTexturesForPrimitives || textureCords != null) {
			renderPolyheadron(verts, textureCords);
		} else {
			renderPolyheadron(verts, null);
		}
	}

	public static void createGround(float[][] altitudes, float wide, float length, float high, float[][][] textureCords) {
		if (altitudes.length > 1) {
			int width = altitudes.length;
			float fwidth = (float) width;
			float quadWidth = wide / (fwidth - 1);
			float quadWid = -wide / 2;
			if (altitudes[0].length > 1) {
				int height = altitudes[0].length;
				float fheight = (float) height;
				float quadLength = length / (fheight - 1);
				float quadLen = -length / 2;
				Vector<float[][]> polygons = new Vector<float[][]>();
				Vector<float[][]> texs = null;
				if (textureCords == null && createDefaultTexturesForPrimitives) {
					texs = new Vector<float[][]>();
				}
				for (int x = 0; x < (width - 1); x++) {
					int nx = x + 1;
					float xo = quadWidth * x;
					float nxo = quadWidth * nx;
					for (int y = 0; y < (height - 1); y++) {
						int ny = y + 1;
						float yo = quadLength * y;
						float nyo = quadLength * ny;

						polygons.add(new float[][] { new float[] { quadWid + xo, quadLen + yo, high * altitudes[x][y] }, new float[] { quadWid + nxo, quadLen + yo, high * altitudes[nx][y] },
								new float[] { quadWid + nxo, quadLen + nyo, high * altitudes[nx][ny] } });

						polygons.add(new float[][] { new float[] { quadWid + xo, quadLen + yo, high * altitudes[x][y] }, new float[] { quadWid + nxo, quadLen + nyo, high * altitudes[nx][ny] },
								new float[] { quadWid + xo, quadLen + nyo, high * altitudes[x][ny] } });

						if (texs != null) {
							texs.add(new float[][] { new float[] { (float) x / fwidth, (float) y / fheight }, new float[] { (float) nx / fwidth, (float) y / fheight },
									new float[] { (float) nx / fwidth, (float) ny / fheight } });

							texs.add(new float[][] { new float[] { (float) x / fwidth, (float) y / fheight }, new float[] { (float) nx / fwidth, (float) ny / fheight },
									new float[] { (float) x / fwidth, (float) ny / fheight } });
						}
					}
				}
				// clean up
				if (createDefaultTexturesForPrimitives || textureCords != null) {
					cleanUpAndRenderPolyheadron(polygons, texs, textureCords);
				} else {
					cleanUpAndRenderPolyheadron(polygons, null, null);
				}
			}
		}
	}

	public static void createTorus(float radius, float width, int sections, float[][][] textureCords) {
		Vector<float[][]> polygons = new Vector<float[][]>();
		Vector<float[][]> texs = null;
		float secs = sections;
		if (textureCords == null && createDefaultTexturesForPrimitives) {
			texs = new Vector<float[][]>();
		}
		// sides
		float angleSize = (float) (Math.PI * 2 / secs);
		for (int n = 0; n < sections; n++) {
			for (int m = 0; m < sections; m++) {
				float rad = radius + width * (float) Math.cos(angleSize * (double) m);
				float ox = (float) Math.cos(angleSize * (double) n) * rad;
				float oy = (float) Math.sin(angleSize * (double) n) * rad;
				float oz = width * (float) Math.sin(angleSize * (double) m);

				float rade = radius + width * (float) Math.cos(angleSize * (double) (m + 1));
				float mx = (float) Math.cos(angleSize * (double) n) * rade;
				float my = (float) Math.sin(angleSize * (double) n) * rade;
				float nx = (float) Math.cos(angleSize * (double) (n + 1)) * rade;
				float ny = (float) Math.sin(angleSize * (double) (n + 1)) * rade;
				float ax = (float) Math.cos(angleSize * (double) (n + 1)) * rad;
				float ay = (float) Math.sin(angleSize * (double) (n + 1)) * rad;
				float nz = width * (float) Math.sin(angleSize * (double) (m + 1));
				polygons.add(new float[][] { new float[] { ox, oy, oz }, new float[] { ax, ay, oz }, new float[] { nx, ny, nz }, new float[] { mx, my, nz } });
				if (texs != null) {
					texs.add(new float[][] { new float[] { 0, 0 }, new float[] { 1, 0 }, new float[] { 1, 1 }, new float[] { 0, 1 } });

					/*
					 * texs.add(new float[][] { new float[] {
					 * ((float)n)/((float)sections),
					 * ((float)m)/((float)sections) }, new float[] {
					 * ((float)(n+1))/((float)sections), 0 }, new float[] {
					 * ((float)(n+1))/((float)sections),
					 * ((float)(m+1))/((float)sections) }, new float[] {
					 * ((float)n)/((float)sections),
					 * ((float)(m+1))/((float)sections) } });
					 */
				}
			}
		}
		// clean up
		if (createDefaultTexturesForPrimitives || textureCords != null) {
			cleanUpAndRenderPolyheadron(polygons, texs, textureCords);
		} else {
			cleanUpAndRenderPolyheadron(polygons, null, null);
		}
	}

	public static void createCylinder(float height, float radius, int sections, float[][][] textureCords) {
		Vector<float[][]> polygons = new Vector<float[][]>();
		Vector<float[][]> texs = null;
		float secs = sections;
		if (textureCords == null && createDefaultTexturesForPrimitives) {
			texs = new Vector<float[][]>();
		}
		// sides
		float angleSize = (float) (Math.PI * 2 / secs);
		for (int n = 0; n < sections; n++) {
			float next = (n + 1) % sections;
			float x = (float) Math.cos(angleSize * n) * radius;
			float nx = (float) Math.cos(angleSize * next) * radius;
			float z = (float) Math.sin(angleSize * n) * radius;
			float nz = (float) Math.sin(angleSize * next) * radius;
			polygons.add(new float[][] { new float[] { x, -height / 2f, z }, new float[] { x, height / 2f, z }, new float[] { nx, height / 2f, nz }, new float[] { nx, -height / 2f, nz } });
			if (texs != null) {
				texs.add(new float[][] { new float[] { 0, 1 }, new float[] { 0, 0 }, new float[] { 1, 0 }, new float[] { 1, 1 } });
			}
		}
		// ends
		// surface normals seem to me
		for (float y = -1; y <= 1; y += 2) {
			Vector<float[]> end = new Vector<float[]>();
			Vector<float[]> endTexs = null;
			if (textureCords == null && createDefaultTexturesForPrimitives) {
				endTexs = new Vector<float[]>();
			}
			// add vertices
			for (int n = sections - 1; n >= 0; n--) {
				// for (int n = 0; n < sections; n++) {
				float x = (float) Math.cos(angleSize * n);
				float z = (float) Math.sin(angleSize * n);
				end.add(new float[] { x * radius, y * height / 2f, z * radius });
				if (endTexs != null) {
					endTexs.add(new float[] { (x + 1) / 2, (z + 1) / 2 });
				}
			}
			// clean up
			if (endTexs != null) {
				float[][] endTexsArray = new float[endTexs.size()][];
				texs.add(endTexs.toArray(endTexsArray));
			}
			float[][] endArray = new float[end.size()][];
			polygons.add(end.toArray(endArray));
		}
		// clean up
		if (createDefaultTexturesForPrimitives || textureCords != null) {
			cleanUpAndRenderPolyheadron(polygons, texs, textureCords);
		} else {
			cleanUpAndRenderPolyheadron(polygons, null, null);
		}
	}

	public static void createCone(float height, float radius, int sections, float[][][] textureCords) {
		Vector<float[][]> polygons = new Vector<float[][]>();
		Vector<float[][]> texs = null;
		float secs = sections;
		if (textureCords == null && createDefaultTexturesForPrimitives) {
			texs = new Vector<float[][]>();
		}
		// sides
		float angleSize = (float) (Math.PI * 2 / secs);
		for (int n = 0; n < sections; n++) {
			float next = (n + 1) % sections;
			float x = (float) Math.cos(angleSize * n) * radius;
			float nx = (float) Math.cos(angleSize * next) * radius;
			float z = (float) Math.sin(angleSize * n) * radius;
			float nz = (float) Math.sin(angleSize * next) * radius;
			polygons.add(new float[][] { new float[] { x, -height / 2f, z }, new float[] { 0, height / 2f, 0 }, new float[] { nx, -height / 2f, nz } });
			if (texs != null) {
				texs.add(new float[][] { new float[] { 0, 0 }, new float[] { 0, 1 }, new float[] { 1, 0 } });
			}
		}
		// end
		float y = -1;
		Vector<float[]> end = new Vector<float[]>();
		Vector<float[]> endTexs = null;
		if (textureCords == null && createDefaultTexturesForPrimitives) {
			endTexs = new Vector<float[]>();
		}
		// add vertices
		for (int n = 0; n < sections; n++) {
			float x = (float) Math.cos(angleSize * n);
			float z = (float) Math.sin(angleSize * n);
			end.add(new float[] { x * radius, y * height / 2f, z * radius });
			if (endTexs != null) {
				endTexs.add(new float[] { (x + 1) / 2, (z + 1) / 2 });
			}
		}
		// clean up
		if (endTexs != null) {
			float[][] endTexsArray = new float[endTexs.size()][];
			texs.add(endTexs.toArray(endTexsArray));
		}
		float[][] endArray = new float[end.size()][];
		polygons.add(end.toArray(endArray));
		// clean up
		if (createDefaultTexturesForPrimitives || textureCords != null) {
			cleanUpAndRenderPolyheadron(polygons, texs, textureCords);
		} else {
			cleanUpAndRenderPolyheadron(polygons, null, null);
		}
	}

	public static void createUVSphere(float radius, int sections, float[][][] textureCords) {
		Vector<float[][]> polygons = new Vector<float[][]>();
		Vector<float[][]> texs = null;
		float secs = sections;
		if (textureCords == null && createDefaultTexturesForPrimitives) {
			texs = new Vector<float[][]>();
		}
		float angleSize = (float) (Math.PI / secs);
		for (int yn = 0; yn < sections; yn++) {
			float realRadius = (float) Math.sin(angleSize * (float) yn) * radius;
			int next = yn + 1;
			float fnext = next;
			float nextRadius = (float) Math.sin(angleSize * fnext) * radius;
			float y = (float) Math.cos(angleSize * (float) yn) * radius;
			float nextY = (float) Math.cos(angleSize * fnext) * radius;
			for (int n = 0; n < sections; n++) {
				next = n + 1;
				float fn = n;
				fnext = next;
				float x = (float) Math.cos(2f * angleSize * fn) * realRadius;
				float nx = (float) Math.cos(2f * angleSize * fnext) * realRadius;
				float z = (float) Math.sin(2f * angleSize * fn) * realRadius;
				float nz = (float) Math.sin(2f * angleSize * fnext) * realRadius;

				float ux = (float) Math.cos(2f * angleSize * fn) * nextRadius;
				float unx = (float) Math.cos(2f * angleSize * fnext) * nextRadius;
				float uz = (float) Math.sin(2f * angleSize * fn) * nextRadius;
				float unz = (float) Math.sin(2f * angleSize * fnext) * nextRadius;

				polygons.add(new float[][] { new float[] { x, y, z }, new float[] { nx, y, nz }, new float[] { unx, nextY, unz }, new float[] { ux, nextY, uz } });
				if (texs != null) {
					texs.add(new float[][] { new float[] { 0, 0 }, new float[] { 1, 0 }, new float[] { 1, 1 }, new float[] { 0, 1 } });
				}
			}
		}
		// render
		if (createDefaultTexturesForPrimitives || textureCords != null) {
			cleanUpAndRenderPolyheadron(polygons, texs, textureCords);
		} else {
			cleanUpAndRenderPolyheadron(polygons, null, null);
		}
	}

	/**
	 * Rounds vertex and texture coordinates from automatically generated
	 * polyheadron
	 * 
	 * @param polygons
	 *            a Vector of float[][] polygons each of which is has vertices
	 *            stored as float[]
	 * @param texs
	 *            automatically generated texture coordinates stored in the same
	 *            structure as polygons
	 * @param textureCords
	 *            texture coordinates passed into the primitive drawing command
	 */
	public static void cleanUpAndRenderPolyheadron(Vector<float[][]> polygons, Vector<float[][]> texs, float[][][] textureCords) {
		// clean up
		float[][][] polygonsArray = new float[polygons.size()][][];
		if (textureCords == null && createDefaultTexturesForPrimitives) {
			float[][][] texsArray = new float[texs.size()][][];
			// round texture cords
			for (int x = 0; x < texs.size(); x++) {
				float[][] tps = texs.get(x);
				for (int y = 0; y < tps.length; y++) {
					float[] tpps = tps[y];
					for (int z = 0; z < tpps.length; z++) {
						tpps[z] = (float) Math.round(tpps[z] * roundOffCorrection) / roundOffCorrection;
					}
				}

			}
			// create array
			textureCords = texs.toArray(texsArray);
		}
		// round polygon cords
		for (int x = 0; x < polygons.size(); x++) {
			float[][] ps = polygons.get(x);
			for (int y = 0; y < ps.length; y++) {
				float[] pps = ps[y];
				for (int z = 0; z < pps.length; z++) {
					pps[z] = (float) Math.round(pps[z] * roundOffCorrection) / roundOffCorrection;
				}
			}

		}
		// crate array
		polygons.toArray(polygonsArray);
		renderPolyheadron(polygonsArray, textureCords);
	}

	/**
	 * Don't forget to bind a texture before calling.
	 * 
	 * @param wide
	 *            Width of the texture.
	 * @param high
	 *            Height of the texture.
	 */
	public static void renderImage(Rectangle sample, int wide, int high) {
		float widef = sample.width;
		float highf = sample.height;
		float maxWidef = wide;
		float maxHighf = high;
		float xTransf = ((float) sample.x) / maxWidef;
		float yTransf = ((float) sample.y) / maxHighf;

		// pre
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glShadeModel(GL11.GL_FLAT);
		// render
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(xTransf, yTransf);
		GL11.glVertex3f(-widef / 2, highf / 2, 0);

		GL11.glTexCoord2f(xTransf + widef / maxWidef, yTransf);
		GL11.glVertex3f(widef / 2, highf / 2, 0);

		GL11.glTexCoord2f(xTransf + widef / maxWidef, yTransf + highf / maxHighf);
		GL11.glVertex3f(widef / 2, -highf / 2, 0);

		GL11.glTexCoord2f(xTransf, yTransf + highf / maxHighf);
		GL11.glVertex3f(-widef / 2, -highf / 2, 0);
		GL11.glEnd();
		// post
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_TEXTURE_2D);

	}

	public static int createTextureFromImage(BufferedImage img) {
		int wide = img.getWidth(null);
		int high = img.getHeight(null);
		ByteBuffer buf = bufferedImageToByteBuffer(img);
		return createTexture(wide, high, buf);
	}

	public static int fillTextureFromImage(int texture, BufferedImage img) {
		int wide = img.getWidth(null);
		int high = img.getHeight(null);
		ByteBuffer buf = bufferedImageToByteBuffer(img);
		return fillTexture(texture, wide, high, buf);
	}

	public static ByteBuffer bufferedImageToByteBuffer(BufferedImage img) {
		int wide = img.getWidth(null);
		int high = img.getHeight(null);

		int type = img.getType();

		int[] byteAsInt = new int[4 * wide * high];
		img.getData().getPixels(0, 0, wide, high, byteAsInt);

		ByteBuffer buf = ByteBuffer.allocateDirect(4 * wide * high);
		if (type == 6 || type == 2) {
			for (int m = 0; m < byteAsInt.length; m++) {

				buf.put((byte) (byteAsInt[m]));
			}
		} else {
			int j;
			for (int m = 0; m < byteAsInt.length; m++) {
				if (m % 4 == 3) {
					// alpha value
					buf.put(Byte.MAX_VALUE);
				} else {
					j = 3 * ((m - m % 4) / 4) + m % 4;
					buf.put((byte) (byteAsInt[j]));
				}

			}
		}
		buf.flip();
		return buf;
	}

	public static int createTexture(int width, int height, ByteBuffer buf) {
		int texture = GL11.glGenTextures();
		fillTexture(texture, width, height, buf);
		return texture;
	}

	public static int fillTexture(int texture, int width, int height, ByteBuffer buf) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
		if (smoothTextures) {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		} else {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		}
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
		return texture;
	}

	public static BufferedImage makeBufferedImage(Image image) {
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		} else {
			if (image.getWidth(null) == -1 || image.getHeight(null) == -1) {
				System.out.println("No such image exists.");
				return null;
			} else {
				BufferedImage img = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
				Graphics2D graph = img.createGraphics();
				graph.drawImage(image, 0, 0, null);
				return img;
			}
		}
	}

	public static void renderCubeFractal(int recursions, float width) {
		Vector<float[][]> polys = new Vector<float[][]>();
		Vector<float[][]> texs = new Vector<float[][]>();
		double drecursions = recursions;
		int sum = (int) (6 * (1 - Math.pow(5, drecursions)) / -4);
		System.out.println(sum + " Polygons Rendered");
		renderCubeFractalSide(polys, texs, recursions, new float[] { 0, 0, 1 }, new float[] { 0, 0, width / 2 }, width);
		renderCubeFractalSide(polys, texs, recursions, new float[] { 0, 0, -1 }, new float[] { 0, 0, -width / 2 }, width);
		renderCubeFractalSide(polys, texs, recursions, new float[] { 0, 1, 0 }, new float[] { 0, width / 2, 0 }, width);
		renderCubeFractalSide(polys, texs, recursions, new float[] { 0, -1, 0 }, new float[] { 0, -width / 2, 0 }, width);
		renderCubeFractalSide(polys, texs, recursions, new float[] { 1, 0, 0 }, new float[] { width / 2, 0, 0 }, width);
		renderCubeFractalSide(polys, texs, recursions, new float[] { -1, 0, 0 }, new float[] { -width / 2, 0, 0 }, width);
		RenderUtils.cleanUpAndRenderPolyheadron(polys, texs, null);
	}

	public static void renderCubeFractalSide(Vector<float[][]> polygons, Vector<float[][]> texs, int recursions, float[] out, float[] position, float width) {
		float[] perpAxisOne;
		float[] perpAxisTwo;
		if (Math.abs(out[0]) == 1) {
			perpAxisOne = new float[] { 0, 1, 0 };
		} else {
			perpAxisOne = new float[] { 1, 0, 0 };
		}
		perpAxisTwo = VectorMath.absVector(VectorMath.crossProduct(perpAxisOne, out));

		float hwidth = width / 2f;
		float twidth = width / 3f;
		float swidth = width / 6f;

		float[] left = VectorMath.multiplyVector(perpAxisOne, -hwidth);
		float[] right = VectorMath.multiplyVector(perpAxisOne, hwidth);
		float[] top = VectorMath.multiplyVector(perpAxisTwo, -hwidth);
		float[] bottom = VectorMath.multiplyVector(perpAxisTwo, hwidth);

		// if(recursions==0){
		polygons.add(new float[][] { VectorMath.addVectors(position, VectorMath.addVectors(left, top)), VectorMath.addVectors(position, VectorMath.addVectors(left, bottom)),
				VectorMath.addVectors(position, VectorMath.addVectors(right, bottom)), VectorMath.addVectors(position, VectorMath.addVectors(right, top)) });
		texs.add(new float[][] { new float[] { 0, 0 }, new float[] { 0, 1 }, new float[] { 1, 1 }, new float[] { 1, 0 } });
		if (recursions > 0) {
			float[] sAxeOne = VectorMath.multiplyVector(perpAxisOne, swidth);
			float[] sAxeTwo = VectorMath.multiplyVector(perpAxisTwo, swidth);
			float[] newCenter = VectorMath.addVectors(position, VectorMath.multiplyVector(out, swidth));

			int nrec = recursions - 1;

			renderCubeFractalSide(polygons, texs, nrec, out, VectorMath.addVectors(position, VectorMath.multiplyVector(out, twidth)), twidth);

			renderCubeFractalSide(polygons, texs, nrec, perpAxisOne, VectorMath.addVectors(newCenter, sAxeOne), twidth);
			renderCubeFractalSide(polygons, texs, nrec, VectorMath.multiplyVector(perpAxisOne, -1), VectorMath.subtractVectors(newCenter, sAxeOne), twidth);

			renderCubeFractalSide(polygons, texs, nrec, perpAxisTwo, VectorMath.addVectors(newCenter, sAxeTwo), twidth);
			renderCubeFractalSide(polygons, texs, nrec, VectorMath.multiplyVector(perpAxisTwo, -1), VectorMath.subtractVectors(newCenter, sAxeTwo), twidth);
		}
	}

}
