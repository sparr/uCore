package io.anuke.ucore.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.I18NBundle;
import io.anuke.ucore.core.Core;

import java.util.Locale;

public class Bundles {

    public static boolean enabled(){
        return Core.bundle != null;
    }

    public static String get(String key, String normal){
        return enabled() && has(key) ? get(key) : normal;
    }

    public static boolean has(String key){
        return !Core.bundle.get(key).contains("???");
    }

    public static String get(String name){
        return Core.bundle.get(name);
    }

    public static String getNotNull(String name){
        String s = get(name);
        if(s.contains("???")){
            throw new NullPointerException("No key with name \"" + name + "\" found!");
        }
        return s;
    }

    public static String format(String key, Object... args){
        return Core.bundle.format(key, args);
    }

}
