package com.keepon.coroutines.bean;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * createBy	 keepon
 */
public class User {
    String name;

    public User(String name) {
        this.name = name;
    }

    @NotNull
    public static User from(@Nullable String string) {
        return new  User("keepon");
    }
}
