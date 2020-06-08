package de.bsautermeister.jump.screens.finish.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public final class PersonFormationFactory {
    private PersonFormationFactory() {}

    public static PersonFormation createRandomFormation() {
        if (MathUtils.random(1) == 0) {
            return createBlockCheersFormation();
        }
        return createLaolaCheersFormation();
    }

    public static PersonFormation createBlockCheersFormation() {
        Array<Array<Person>> result = new Array<>();
        for (int i = 0; i < 9; ++i) {
            result.add(createBlockCheersRow());
        }
        return new PersonFormation(result);
    }

    private static Array<Person> createBlockCheersRow() {
        return Array.with(
                new Person(0f, createCheersAnimation()),
                new Person(0f, createCheersAnimation()),
                new Person(0f, createCheersAnimation()),
                new Person(0f, createCheersAnimation()),
                new Person(0f, createCheersAnimation()),
                new Person(0f, createCheersAnimation()),
                new Person(0f, createCheersAnimation()),
                new Person(0f, createCheersAnimation()),
                Person.empty(),
                new Person(1f, createCheersAnimation()),
                new Person(1f, createCheersAnimation()),
                new Person(1f, createCheersAnimation()),
                new Person(1f, createCheersAnimation()),
                new Person(1f, createCheersAnimation()),
                new Person(1f, createCheersAnimation()),
                new Person(1f, createCheersAnimation()),
                new Person(1f, createCheersAnimation()),
                Person.empty(),
                new Person(2f, createCheersAnimation()),
                new Person(2f, createCheersAnimation()),
                new Person(2f, createCheersAnimation()),
                new Person(2f, createCheersAnimation()),
                new Person(2f, createCheersAnimation()),
                new Person(2f, createCheersAnimation()),
                new Person(2f, createCheersAnimation()),
                new Person(2f, createCheersAnimation())
        );
    }

    public static PersonFormation createLaolaCheersFormation() {
        Array<Array<Person>> result = new Array<>();
        for (int i = 0; i < 9; ++i) {
            result.add(createLaolaCheersRow(0.1f));
        }
        return new PersonFormation(result);
    }

    private static Array<Person> createLaolaCheersRow(float delay) {
        Array<Person> result = new Array<>(26);
        for (int i = 0; i < 26; ++i) {
            if ((i + 1) % 9 == 0) {
                result.add(Person.empty());
                continue;
            }
            result.add(new Person(i * delay, createCheersAnimation()));
        }
        return result;
    }

    private static Animatable createCheersAnimation() {
        return new PathAnimation(0f, true, Array.with(
                new PathAnimation.Item(1.0f, 1f),
                new PathAnimation.Item(2.0f, 1f),
                new PathAnimation.Item(1.0f, 0f),
                new PathAnimation.Item(2.0f, 0f),
                new PathAnimation.Item(1.0f, 1f),
                new PathAnimation.Item(2.0f, 1f),
                new PathAnimation.Item(1.0f, 0f),
                new PathAnimation.Item(2.0f, 0f)
        ));
    }
}
