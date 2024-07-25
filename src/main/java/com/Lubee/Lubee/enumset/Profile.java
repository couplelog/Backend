package com.Lubee.Lubee.enumset;

import lombok.Getter;

@Getter
public enum Profile {
    a("a"),
    b("b"),
    c("c"),
    d("d"),
    e("e"),
    f("f"),
    g("g"),
    h("h");


    private final String ProfileName;

    Profile(String profileName) {
        this.ProfileName = profileName;
    }

}
