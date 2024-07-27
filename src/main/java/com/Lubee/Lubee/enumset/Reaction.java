package com.Lubee.Lubee.enumset;

import lombok.Getter;

@Getter
public enum Reaction {
        heart("heart"),
        honey("honey"),
        smile("smile"),
        bag("bag"),
        thumb("thumb");

        private final String ReactionName;

        Reaction(String reactionName)
        {
                this.ReactionName = reactionName;
        }


}
