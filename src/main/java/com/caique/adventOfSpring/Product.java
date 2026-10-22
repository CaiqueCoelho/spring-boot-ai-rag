package com.caique.adventOfSpring;

public record Product(
        int id,
        String title,
        double price,
        String description,
        String category,
        String image,
        Rating rating
) {

    public record Rating(double rate, int count){}
}
