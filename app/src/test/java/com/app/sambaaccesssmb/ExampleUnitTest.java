package com.app.sambaaccesssmb;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void getFibonacci() {
        int lastDigit = 1;
        int updatedDigit = 0;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            stringBuilder.append("," + updatedDigit);
            updatedDigit = updatedDigit + lastDigit;
            lastDigit = updatedDigit;
        }
        System.out.println(stringBuilder);

        int mLastDigit = 1;
        List<Integer> integerList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            if (integerList.size() >= 2) {
                mLastDigit = integerList.get(i - 1) + integerList.get(i - 2);
            }
            integerList.add(mLastDigit);
        }
        System.out.println(integerList.toString());
    }

    @Test
    public void printStars(){

        for (int i = 1; i <= 5; i++) {
            for (int j = 0; j < i; j++) {
                System.out.print("*");
            }
            System.out.println();
        }

        for (int i = 5; i >= 1; i--) {
            for (int j = i; j >0; j--) {
                System.out.print("*");
            }
            System.out.println();
        }
    }
}