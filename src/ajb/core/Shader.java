package ajb.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;

public class Shader {
	private int shader = 0;
	private int vertShader = 0;
	private int fragShader = 0;

	public Shader(URL vert, URL frag) {
		shader = ARBShaderObjects.glCreateProgramObjectARB();
		if(shader!=0){
			vertShader = createVertShader(vert);
			//createVertShader("shaders/screen.vert");
			fragShader = createFragShader(frag);
			//createFragShader("shaders/screen.frag");
			if(vertShader!=0&&fragShader!=0){
				ARBShaderObjects.glAttachObjectARB(shader, vertShader);
	            ARBShaderObjects.glAttachObjectARB(shader, fragShader);
	            ARBShaderObjects.glLinkProgramARB(shader);
	            ARBShaderObjects.glValidateProgramARB(shader);
			}
		}
	}
	
	public int getShader(){
		return shader;
	}

	private int createVertShader(URL filename) {
		// vertShader will be non zero if succefully created

		vertShader = ARBShaderObjects.glCreateShaderObjectARB(ARBVertexShader.GL_VERTEX_SHADER_ARB);
		// if created, convert the vertex shader code to a String
		if (vertShader == 0) {
			return 0;
		} else {
			String vertexCode = loadText(filename);
			vertexCode=vertexCode.replace("﻿", "");
			/*
			 * associate the vertex code String with the created vertex shader
			 * and compile
			 */
			ARBShaderObjects.glShaderSourceARB(vertShader, vertexCode);
			ARBShaderObjects.glCompileShaderARB(vertShader);
			
			return vertShader;
		}
	}

	// same as per the vertex shader except for method syntax
	private int createFragShader(URL filename) {

		fragShader = ARBShaderObjects.glCreateShaderObjectARB(ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);
		if (fragShader == 0) {
			return 0;
		} else {
			String fragCode = loadText(filename);
			fragCode=fragCode.replace("﻿", "");
			ARBShaderObjects.glShaderSourceARB(fragShader, fragCode);
			ARBShaderObjects.glCompileShaderARB(fragShader);
			
			return fragShader;
		}
	}
	
	private String loadText(URL url){
		String str="";
		//read
		try {
			InputStream in=url.openStream();
			while(true){
				int val=in.read();
				if(val!=-1){
					str+=(char)val;
				}else{
					break;
				}
			}
			in.close();
		} catch (IOException e) {
			System.out.println("Failed to load: "+url.toString());
		}
		return str;
	}
	
}
