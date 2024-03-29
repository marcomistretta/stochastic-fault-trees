package test.swe.smft.utilities.Statistic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import src.swe.smft.utilities.QuantizedSample;
import src.swe.smft.utilities.Statistic;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StatisticTest {

    private final ArrayList<ArrayList<QuantizedSample>> quantizedResults = new ArrayList<>();
    private final int N = 10; // numero di simulazioni
    private final int times = 100; // numero di istanti

    @BeforeEach
    public void setUp() throws Exception {
        ArrayList<Boolean> basicEvents = new ArrayList<>();
        basicEvents.add(true); // non ci servono le foglie
        QuantizedSample quantizedSample = new QuantizedSample(true, basicEvents); // la prima posizione è quella di nostro interess
        ArrayList<QuantizedSample> internArray = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            internArray.clear();
            for (int j = 0; j < times; j++) {
                internArray.add(quantizedSample); // ogni arrayList interno sarà costituito da "times" pair
            }
            quantizedResults.add(internArray);
        }
    }

    @Test
    @DisplayName("Check Sample Mean Calc")
    public void testSampleMean() {
        for (int j = 0; j < times; j++) {
            assertEquals(1, Statistic.sampleMean(quantizedResults)[j], "Sample mean of a dataset of true should be one");
        }
    }

    // @RepeatedTest(5)
    @Test
    @DisplayName("Check Standard Deviation Calc")
    public void testStandardDeviation() {
        for (int j = 0; j < times; j++) {
            assertEquals(0, Statistic.sampleStandardDeviation(quantizedResults, Statistic.sampleMean(quantizedResults))[j], "Standard deviation of a dataset of true should be zero");
        }
    }
}
