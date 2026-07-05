package com.drivemaster.drivemastermain.domain;

public enum ListingType {
    SALE,
    RENT,
    BOTH;

    public boolean supports(OrderType orderType) {
        return switch (orderType) {
            case PURCHASE -> this == SALE || this == BOTH;
            case RENTAL -> this == RENT || this == BOTH;
        };
    }
}
