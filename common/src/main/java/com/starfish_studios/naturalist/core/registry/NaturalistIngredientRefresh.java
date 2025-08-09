package com.starfish_studios.naturalist.core.registry;

import com.starfish_studios.naturalist.common.entity.*;

/**
 * Manages ingredient refresh for all naturalist entities after tags are loaded
 */
public class NaturalistIngredientRefresh {
    
    public static void refreshAllIngredients() {
        Alligator.refreshIngredients();
        Moose.refreshIngredients();
        Giraffe.refreshIngredients();
        Snake.refreshIngredients();
        // Duck.refreshIngredients() - removed
        Boar.refreshIngredients();
        Lizard.refreshIngredients();
        Tortoise.refreshIngredients();
        Bird.refreshIngredients();
    }
}