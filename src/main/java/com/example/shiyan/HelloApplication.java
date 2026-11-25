package com.example.shiyan;

import jakarta.ws.rs.core.Application;

import java.util.HashSet;
import java.util.Set;

/**
 * JAX-RS 应用入口：由 web.xml 中的 Jersey ServletContainer 加载。
 * 访问路径：/shiyan_war_exploded/api/hello-world
 */
public class HelloApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(HelloResource.class);
        return classes;
    }
}
