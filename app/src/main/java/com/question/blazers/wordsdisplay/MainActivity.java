package com.question.blazers.wordsdisplay;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.question.blazers.wordsdisplay.widget.WordsDisplayView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private WordsDisplayView mWordsDisplayView;

    private List<RandomTest> mRandomTestList = new ArrayList<>();

    private Random mRandom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWordsDisplayView = (WordsDisplayView) findViewById(R.id.words);
        findViewById(R.id.random).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                randomText();
            }
        });

        // 初始化测试
        mRandom = new Random();
        mRandomTestList.add(new RandomTest(
                "Aspernatur saepe distinctio eum rerum expedita. Hic iste ducimus autem rerum consequatur impedit ab. Adipisci animi consectetur minus non temporibus aut magni dolorem.",
                "eum", "ab"
        ));

        mRandomTestList.add(new RandomTest(
                "Quae nam commodi vel labore et vel dolores mollitia. Aut unde nemo sit tempora possimus. Accusamus minima quibusdam non. Cupiditate architecto ipsa omnis et est aut ipsa vitae.",
                "vel dolores mollitia", "tempora"
        ));

        mRandomTestList.add(new RandomTest(
                "Fugiat laborum aliquid vitae perferendis voluptatem. Impedit quidem rerum illo at minus. Dicta sint et laboriosam dignissimos sed.",
                "voluptatem", "Dicta sint"
        ));

        // Test
        List<String> highlighted = new ArrayList<>();
        highlighted.add("is");
        highlighted.add("very stupid");
        mWordsDisplayView.setDisplayedText("randomText  nextInt  mRandomTestList  build.gradle  This is a very stupid boy.", highlighted);
    }

    private void randomText() {
        int target = Math.abs(mRandom.nextInt()) % mRandomTestList.size();
        RandomTest randomTest = mRandomTestList.get(target);
        mWordsDisplayView.setDisplayedText(randomTest.text, randomTest.highlighted);
    }

    private class RandomTest {

        private String text;
        private List<String> highlighted;

        public RandomTest(String text, String ... highlighted) {
            this.text = text;
            this.highlighted = Arrays.asList(highlighted);
        }
    }
}
