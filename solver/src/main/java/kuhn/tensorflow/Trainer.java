package kuhn.tensorflow;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.WorkspaceMode;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.RmsProp;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.ArrayList;
import java.util.List;

public class Trainer {
    public static final int NB_INPUTS = 7;
    List<KuhnDataSet> trainingSets = new ArrayList<>();
    private MultiLayerNetwork model;

    public static void main(String[] args) {
        KuhnIterator kuhnIterator = new KuhnIterator(10000, 3);
        kuhnIterator.cfrIterationsExternal();
    }

    public Trainer() {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .trainingWorkspaceMode(WorkspaceMode.ENABLED).inferenceWorkspaceMode(WorkspaceMode.ENABLED)
                .seed(123L)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .weightInit(WeightInit.XAVIER)
                .updater(new RmsProp.Builder().rmsDecay(0.95).learningRate(1e-2).build())
                .list()
                .layer(new LSTM.Builder().name("lstm1")
                        .activation(Activation.TANH).nIn(NB_INPUTS).nOut(20).build())
                .layer(new LSTM.Builder().name("lstm2")
                        .activation(Activation.TANH).nOut(1).build())
                .layer(new RnnOutputLayer.Builder().name("output")
                        .activation(Activation.IDENTITY).nOut(1).lossFunction(LossFunctions.LossFunction.MSE)
                        .build())
                .build();

        model = new MultiLayerNetwork(conf);
        model.init();
    }

    public void trainWithBatch() {
        int maxHistoryLength = trainingSets.stream().map(ds -> ds.history.size()).reduce(Integer::max).orElseThrow();
        INDArray features = Nd4j.create(new int[]{trainingSets.size(), NB_INPUTS, maxHistoryLength}, DataType.DOUBLE);
        INDArray featuresMask = Nd4j.zeros(new int[]{trainingSets.size(), maxHistoryLength}, DataType.DOUBLE);
        INDArray labels = Nd4j.create(new int[]{trainingSets.size(), 1, maxHistoryLength}, DataType.DOUBLE);
        INDArray labelsMask = Nd4j.zeros(new int[]{trainingSets.size(), maxHistoryLength}, DataType.DOUBLE);
        for (int j=0;j<trainingSets.size();j++) {
            KuhnDataSet dataSet = trainingSets.get(j);
            int diff = maxHistoryLength - dataSet.history.size();
            labelsMask.putScalar(new int[]{j, maxHistoryLength-1}, 1);
            for (int i = 0; i < dataSet.history.size(); i++) {
                featuresMask.putScalar(new int[]{j, i+diff}, 1);
                features.putScalar(new int[]{j, 0, i+diff}, dataSet.player1);
                features.putScalar(new int[]{j, 1, i+diff}, dataSet.player2);
                features.putScalar(new int[]{j, 2, i+diff}, dataSet.holeCard == 0 ? 1 : 0);
                features.putScalar(new int[]{j, 3, i+diff}, dataSet.holeCard == 1 ? 1 : 0);
                features.putScalar(new int[]{j, 4, i+diff}, dataSet.holeCard == 2 ? 1 : 0);
                features.putScalar(new int[]{j, 5, i+diff}, dataSet.history.get(i) == 0 ? 1 : 0);
                features.putScalar(new int[]{j, 6, i+diff}, dataSet.history.get(i) == 1 ? 1 : 0);
                labels.putScalar(new int[]{j, 0, i+diff}, dataSet.value);
            }
        }

        model.fit(new DataSet(features, labels, featuresMask, labelsMask));
        trainingSets.clear();
    }

    public double predict(KuhnDataSet dataSet) {
        INDArray features = Nd4j.create(new int[]{1, NB_INPUTS, 3}, DataType.DOUBLE);
        for (int i = 0; i < dataSet.history.size(); i++) {
            features.putScalar(new int[]{0, 0, i}, dataSet.player1);
            features.putScalar(new int[]{0, 1, i}, dataSet.player2);
            features.putScalar(new int[]{0, 2, i}, dataSet.holeCard == 0 ? 1 : 0);
            features.putScalar(new int[]{0, 3, i}, dataSet.holeCard == 1 ? 1 : 0);
            features.putScalar(new int[]{0, 4, i}, dataSet.holeCard == 2 ? 1 : 0);
            features.putScalar(new int[]{0, 5, i}, dataSet.history.get(i) == 0 ? 1 : 0);
            features.putScalar(new int[]{0, 6, i}, dataSet.history.get(i) == 1 ? 1 : 0);
        }
        INDArray output2 = model.output(features);
        //System.out.println(features + "" + output2);
        return output2.getDouble(0, 0, 2);
    }

    public void addTrainingSet(KuhnDataSet dataSet) {
        trainingSets.add(dataSet);
    }
}
