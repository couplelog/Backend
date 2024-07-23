package com.Lubee.Lubee.enumset;

import lombok.Getter;

@Getter
public enum Profile {
    a("1"),
    b("2"),
    c("3"),
    d("4"),
    e("5"),
    f("6"),
    g("7"),
    h("8");


    private final String ProfileName;

    Profile(String profileName) {
        this.ProfileName = profileName;
    }

}
