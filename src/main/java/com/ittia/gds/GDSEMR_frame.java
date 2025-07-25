package com.ittia.gds;

// 필요한 JavaFX 및 유틸리티 클래스 임포트
import com.ittia.gds.db.DatabaseManager;
import com.ittia.gds.ui.mainframe.buttons.GDSFrameButtonExe;
import com.ittia.gds.ui.mainframe.changestring.AbbreviationManagerUI;
import com.ittia.gds.ui.mainframe.changestring.AbbreviationsMain;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class GDSEMR_frame extends Application {

    // --- 상수 정의 ---
    // 프레임(창)의 고정된 너비와 높이
    private static final double FRAME_WIDTH = 1350;
    private static final double FRAME_HEIGHT = 900;

    // 텍스트 영역의 제목들
    public static final String[] TEXT_AREA_TITLES = {
        "CC>", "PI>", "ROS>", "PMH>", "S>",
        "O>", "Physical Exam>", "A>", "P>", "Comment>"
    };

    // UI 컴포넌트 참조 (static으로 선언하여 외부에서 접근 가능)
    public static TextArea[] textAreas;
    public static TextArea tempOutputArea;
    public static TextField gradientInputField; // 현재 코드에서 사용되지 않음

    // --- 애플리케이션 라이프사이클 (메인 진입점) ---
    @Override
    public void start(Stage primaryStage) {
        // 텍스트 영역 초기화
        textAreas = new TextArea[TEXT_AREA_TITLES.length];
        
        primaryStage.setMinWidth(FRAME_WIDTH);
        primaryStage.setMinHeight(FRAME_HEIGHT);
        primaryStage.setResizable(true);

        
        // 텍스트 영역이 한 번 클리어되었는지 추적하는 프로퍼티
        BooleanProperty cleared = new SimpleBooleanProperty(false);

        // 각 텍스트 영역 생성 및 설정
        for (int i = 0; i < TEXT_AREA_TITLES.length; i++) {
            TextArea ta = new TextArea();
            ta.setPromptText(TEXT_AREA_TITLES[i]); // 프롬프트 텍스트 설정 (예: "CC>")
            ta.setFont(Font.font("Consolas", 13)); // 폰트 설정
            ta.setWrapText(true); // 자동 줄 바꿈
            ta.setPrefRowCount(3); // 초기 행 수
            ta.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-border-radius: 5px;"); // 스타일 설정

            // 텍스트 영역 포커스 리스너: 첫 포커스 시 모든 텍스트 영역 클리어
            ta.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal && !cleared.get()) {
                    for (TextArea t : textAreas) {
                        if (t != null) t.clear();
                    }
                    cleared.set(true); // 클리어 상태를 true로 변경하여 다시 클리어되지 않도록 함
                }
            });
            textAreas[i] = ta;
        }

        // 임시 출력 영역 설정
        tempOutputArea = new TextArea();
        tempOutputArea.setEditable(true); // 편집 가능 여부 (현재는 true)
        tempOutputArea.setPromptText("Output"); // 프롬프트 텍스트
        tempOutputArea.setFont(Font.font("Consolas", 13)); // 폰트
        tempOutputArea.setWrapText(true); // 자동 줄 바꿈
        tempOutputArea.setPrefRowCount(40); // 초기 행 수
        tempOutputArea.setStyle("-fx-border-color: #d0d0d0; -fx-border-width: 1px; -fx-border-radius: 5px;"); // 스타일

        // --- 외부 의존성 주입 및 초기화 ---
        DatabaseManager dbManager = new DatabaseManager();
        // AbbreviationsMain은 textAreas와 tempOutputArea를 사용하여 약어 변환 처리
        AbbreviationsMain abbreviationHandler = new AbbreviationsMain(textAreas, tempOutputArea, TEXT_AREA_TITLES);
        // AbbreviationManagerUI는 약어 관리 UI를 담당하며, abbreviationHandler와 dbManager 사용
        AbbreviationManagerUI abbreviationManagerUI = new AbbreviationManagerUI(abbreviationHandler, dbManager);

        // 각 텍스트 영역의 텍스트 변경 리스너: 입력 시 출력 영역 업데이트
        for (TextArea ta : textAreas) {
            ta.textProperty().addListener((obs, oldVal, newVal) -> {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < TEXT_AREA_TITLES.length; j++) {
                    String content = textAreas[j].getText();
                    if (content != null && !content.trim().isEmpty()) {
                        sb.append(TEXT_AREA_TITLES[j]).append(" ").append(content.trim()).append("\n\n");
                    }
                }
                tempOutputArea.setText(sb.toString().trim());
            });
        }

        // --- UI 레이아웃 구성 ---

        // 우측 입력 그리드 패인 설정
        GridPane rightInputGrid = new GridPane();
        rightInputGrid.setHgap(15); // 수평 간격
        rightInputGrid.setVgap(10); // 수직 간격
        rightInputGrid.setPadding(new Insets(15)); // 내부 패딩
        // 스타일 (배경색, 테두리 반경, 그림자 효과)
        rightInputGrid.setStyle("-fx-background-color: #f5f5f5; -fx-border-radius: 10px; -fx-background-radius: 10px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        // 텍스트 영역들을 그리드 패인에 추가
        for (int i = 0; i < TEXT_AREA_TITLES.length; i++) {
            Label lbl = new Label(TEXT_AREA_TITLES[i]); // 제목 레이블
            lbl.setFont(Font.font("Arial", FontWeight.BOLD, 14)); // 폰트
            lbl.setTextFill(Color.web("#333333")); // 텍스트 색상
            VBox section = new VBox(3, lbl, textAreas[i]); // 레이블과 텍스트 영역을 VBox로 묶음
            VBox.setVgrow(textAreas[i], Priority.ALWAYS); // 텍스트 영역이 수직으로 확장되도록 설정
            GridPane.setConstraints(section, i % 2, i / 2); // 그리드 위치 설정 (2열로 배치)
            rightInputGrid.getChildren().add(section);
        }

        // 그리드 패인의 열 제약 조건 설정 (두 열 모두 수평으로 확장)
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        rightInputGrid.getColumnConstraints().addAll(col1, col2);

        // 그리드 패인의 행 제약 조건 설정 (5개 행 모두 수직으로 확장, 각 20%)
        for (int i = 0; i < 5; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(Priority.ALWAYS);
            rc.setPercentHeight(20); // 각 행이 전체 높이의 20%를 차지
            rightInputGrid.getRowConstraints().add(rc);
        }

        // 좌측 출력 패인 설정
        VBox leftOutputPane = new VBox();
        leftOutputPane.setPadding(new Insets(15)); // 패딩
        // 배경에 선형 그라데이션 적용
        LinearGradient gradient = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(230, 245, 230)), // 시작 색상 (연한 녹색)
                new Stop(1, Color.rgb(200, 230, 200))  // 끝 색상 (진한 녹색)
        );
        leftOutputPane.setBackground(new javafx.scene.layout.Background(new javafx.scene.layout.BackgroundFill(gradient, new javafx.scene.layout.CornerRadii(10), Insets.EMPTY)));
        leftOutputPane.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0);"); // 그림자 효과

        // 출력 영역을 스크롤 패인에 넣고 설정
        ScrollPane outSp = new ScrollPane(tempOutputArea);
        outSp.setFitToWidth(true); // 너비를 부모에 맞춤
        outSp.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS); // 항상 수직 스크롤바 표시
        VBox.setVgrow(outSp, Priority.ALWAYS); // 스크롤 패인이 수직으로 확장되도록 설정
        leftOutputPane.getChildren().add(outSp);

        // 좌측 출력 패인과 우측 입력 그리드를 분할 패인으로 연결
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(leftOutputPane, rightInputGrid);
        splitPane.setDividerPositions(0.5); // 분할 비율 50:50

        // ======================= 버튼 섹션 시작 =======================

        // 버튼 실행기 인스턴스 생성 (스테이지, 텍스트 영역, 출력 영역 전달)
        GDSFrameButtonExe buttonExecutor = new GDSFrameButtonExe(primaryStage, textAreas, tempOutputArea);

        // --- 상단 패널 (North Panel) ---
        HBox northPanel = new HBox(15); // 요소 간 간격 15
        northPanel.setPadding(new Insets(10, 15, 10, 15)); // 패딩
        northPanel.setAlignment(Pos.CENTER_RIGHT); // 우측 정렬
        northPanel.setStyle("-fx-background-color: #e8e8e8;"); // 배경색

        // "Manage Abbreviations" 버튼
        Button manageAbbrBtn = new Button("Manage Abbreviations");
        manageAbbrBtn.setStyle("-fx-background-color: #f0ad4e; -fx-text-fill: white; -fx-font-weight: bold;"); // 주황색 스타일
        manageAbbrBtn.setOnAction(e -> abbreviationManagerUI.display()); // 약어 관리 UI 표시
        northPanel.getChildren().add(manageAbbrBtn);

        // "ICD11" 버튼
        Button icdButton = new Button("ICD11");
        icdButton.setStyle("-fx-background-color: #5bc0de; -fx-text-fill: white; -fx-font-weight: bold;"); // 파란색 스타일
        icdButton.setOnAction(e -> System.out.println("ICD11 button clicked."));
        northPanel.getChildren().add(icdButton);

        // "Laboratory" 버튼
        Button labButton = new Button("Laboratory");
        labButton.setStyle("-fx-background-color: #5bc0de; -fx-text-fill: white; -fx-font-weight: bold;"); // 파란색 스타일
        labButton.setOnAction(e -> System.out.println("Laboratory button clicked."));
        northPanel.getChildren().add(labButton);

        // 상단 패널의 주요 기능 버튼들 (SaveRescue, Load, Clear, Exit)
        String[] northButtonTitles = {"SaveRescue", "Load", "Clear","Exit"};
        for (String title : northButtonTitles) {
            // 녹색 계열 스타일 버튼 생성
            Button btn = createStyledButton(title, "#5cb85c", "#4cae4c");
            buttonExecutor.attach(btn, title); // 버튼에 액션 연결
            northPanel.getChildren().add(btn);
        }

        // --- 하단 패널 (South Panel) ---
        HBox southPanel = new HBox(15); // 요소 간 간격 15
        southPanel.setPadding(new Insets(10, 15, 10, 15)); // 패딩
        southPanel.setAlignment(Pos.CENTER_LEFT); // 좌측 정렬
        southPanel.setStyle("-fx-background-color: #e8e8e8;"); // 배경색

        // 하단 패널의 주요 기능 버튼들 (Save, Load, Clear, CE, Submit)
        String[] southButtonTitles = {"Save", "Load", "Clear", "CE", "Submit"};
        for (String title : southButtonTitles) {
            // 파란색 계열 스타일 버튼 생성
            Button btn = createStyledButton(title, "#0275d8", "#025aa5");
            buttonExecutor.attach(btn, title); // 버튼에 액션 연결
            southPanel.getChildren().add(btn);
        }

        // ======================== 버튼 섹션 끝 ========================

        // 메인 BorderPane에 모든 패널 배치
        BorderPane root = new BorderPane();
        root.setCenter(splitPane); // 중앙에 분할 패인
        root.setTop(northPanel);   // 상단에 북쪽 패널
        root.setBottom(southPanel); // 하단에 남쪽 패널
        root.setStyle("-fx-background-color: #ffffff;"); // 전체 배경색 흰색

        // 이 부분에서 Frame Size가 명시적으로 설정되고 있습니다.
        // GDSEMR_frame 창이 작게 열리지 않도록 FRAME_WIDTH와 FRAME_HEIGHT를 사용합니다.
        root.setPrefSize(FRAME_WIDTH, FRAME_HEIGHT); // BorderPane의 선호 크기 설정

        // Scene 생성 (root BorderPane과 미리 정의된 FRAME_WIDTH, FRAME_HEIGHT 사용)
        Scene scene = new Scene(root, FRAME_WIDTH, FRAME_HEIGHT);
        primaryStage.setTitle("GDS EMR Interface for Physician - Enhanced"); // 창 제목 설정
        primaryStage.setScene(scene); // Scene을 Stage에 설정
        primaryStage.setOnCloseRequest(e -> System.exit(0)); // 창 닫기 버튼 클릭 시 애플리케이션 종료
        primaryStage.show(); // Stage 표시
    }

    // --- 헬퍼 메서드 ---

    /**
     * 특정 스타일의 버튼을 생성합니다.
     * @param text 버튼에 표시될 텍스트.
     * @param baseColor 버튼의 기본 배경색 (CSS 형식).
     * @param hoverColor 마우스 오버 시 버튼의 배경색 (CSS 형식).
     * @return 스타일이 적용된 Button 객체.
     */
    private Button createStyledButton(String text, String baseColor, String hoverColor) {
        Button button = new Button(text);
        button.setPrefWidth(80); // 버튼의 선호 너비
        // 기본 스타일 문자열
        final String baseStyle = String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5px; -fx-background-radius: 5px;", baseColor);
        // 마우스 오버 시 스타일 문자열
        final String hoverStyle = String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5px; -fx-background-radius: 5px;", hoverColor);
        button.setStyle(baseStyle); // 초기 스타일 적용
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle)); // 마우스 진입 시 스타일 변경
        button.setOnMouseExited(e -> button.setStyle(baseStyle));   // 마우스 이탈 시 스타일 복원
        return button;
    }

    // --- 메인 메서드 ---

    /**
     * JavaFX 애플리케이션을 시작하는 메인 메서드.
     * @param args 명령줄 인수 (이 애플리케이션에서는 사용되지 않음).
     */
    public static void main(String[] args) {
        launch(args); // 이 메서드가 start() 메서드를 호출합니다.
    }
}