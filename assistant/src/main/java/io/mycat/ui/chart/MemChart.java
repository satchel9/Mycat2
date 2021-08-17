package io.mycat.ui.chart;

import io.mycat.ui.MonitorService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.SneakyThrows;

public class MemChart extends Application {

    private LineChart<Number, Number> chart;

    private XYChart.Series<Number, Number> dataSeries;

    private NumberAxis xAxis;
    //
    private Timeline animation;

    private double sequence = 0;

    private double y = 1;

    private final int MAX_DATA_POINTS = 60;
    private final MonitorService monitorService;

    public MemChart(MonitorService monitorService) {
        this.monitorService = monitorService;
        // create timeline to add new data every 60th of second
        animation = new Timeline();
        animation.getKeyFrames()
                .add(new KeyFrame(Duration.millis(1000),
                        (ActionEvent actionEvent) -> {
                            plotTime();
                        }));
        animation.setCycleCount(Animation.INDEFINITE);
    }

    public Parent createContent() {

        xAxis = new NumberAxis(0, 60, 1);
        final NumberAxis yAxis = new NumberAxis(0, 100, 1);
        chart = new LineChart<>(xAxis, yAxis);

        // setup chart
        chart.setAnimated(true);
        chart.setLegendVisible(false);
        chart.setTitle("内存");
        xAxis.setLabel("时间(s)");
        xAxis.setForceZeroInRange(false);

        yAxis.setLabel("使用率");
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis, "", "%"));

        // add starting data
        dataSeries = new XYChart.Series<>();
        dataSeries.setName("Data");

        // create some starting data
        dataSeries.getData()
                .add(new XYChart.Data<Number, Number>(++sequence, y));

        chart.getData().add(dataSeries);

        return chart;
    }

    @SneakyThrows
    private void plotTime() {
        monitorService.fetchInstanceEntry().onSuccess(entry ->
                Platform.runLater(() -> {
                    if (sequence > 60) {
                        sequence = -1;
                    }
                    dataSeries.getData().add(new XYChart.Data<Number, Number>(++sequence, entry.getMem() * 100));
                    if (sequence > MAX_DATA_POINTS) {
                        dataSeries.getData().remove(0);
                    }

                }));
    }

    public void play() {
        animation.play();
    }

    @Override
    public void stop() {
        animation.pause();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(createContent()));
        primaryStage.setTitle("MEMORY Chart");
        primaryStage.show();
        play();
    }

    /**
     * Java main for when running without JavaFX launcher
     */
    public static void main(String[] args) {
        launch(args);
    }


}