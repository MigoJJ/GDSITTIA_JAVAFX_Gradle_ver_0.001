package com.ittia.gds;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GDSittiaEntry extends Application {

    // --- Constants ---
    private static final String[] BUTTON_NAMES = {
        "Log In", "Ittia Start", "Prologue", "Version Information", "Rescue", "Quit"
    };

    // --- Application Lifecycle ---
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("ITTIA Launcher");

        // 프레임(창) 크기 유지 문제를 해결하기 위한 설정 시작 ---------------------------------
        // 1. 창의 크기가 사용자에 의해 변경되지 않도록 고정합니다.
        primaryStage.setResizable(false);

        // 2. 창의 최소 너비와 높이를 설정하여, 특정 크기 이하로 줄어들지 않도록 합니다.
        //    (setResizable(false)와 함께 사용하면 명시된 Scene 크기를 효과적으로 유지하는 데 도움이 됩니다.)
        primaryStage.setMinWidth(320);  // Scene 너비(300)보다 약간 크게 설정하여 여유 공간 확보
        primaryStage.setMinHeight(380); // Scene 높이(350)보다 약간 크게 설정하여 여유 공간 확보
        // ------------------------------------------------------------------

        VBox root = new VBox(10); // 요소들 간의 간격 10
        root.setStyle("-fx-padding: 20; -fx-alignment: center;"); // 패딩 20, 중앙 정렬

        createButtons(root); // VBox에 버튼들을 추가

        // Scene 크기를 설정합니다. VBox의 패딩(좌우 40)과 버튼 너비(200)를 고려할 때,
        // 너비 300은 적절하지만, 필요에 따라 약간 더 늘릴 수 있습니다 (예: 320).
        // 높이 350은 6개 버튼, 간격, 패딩을 수용하기에 충분합니다.
        Scene scene = new Scene(root, 300, 350);
        primaryStage.setScene(scene);
        primaryStage.show(); // 런처 창을 표시
    }

    // --- UI Component Creation ---

    /**
     * BUTTON_NAMES 배열을 기반으로 버튼들을 생성하고 제공된 VBox에 추가합니다.
     * 각 버튼은 해당 기능에 대한 액션 핸들러로 구성됩니다.
     * @param root 버튼이 추가될 VBox.
     */
    private void createButtons(VBox root) {
        for (String name : BUTTON_NAMES) {
            Button button = new Button(name);
            button.setPrefWidth(200); // 일관성을 위해 선호 너비 200 설정

            // 각 버튼에 대한 액션 정의
            button.setOnAction(e -> {
                try {
                    handleButtonPress(name);
                } catch (IOException ex) {
                    ex.printStackTrace(); // 디버깅을 위해 전체 스택 트레이스 로깅
                    showAlert(AlertType.ERROR, "오류", "오류 발생",
                              "요청한 파일을 열거나 애플리케이션을 시작할 수 없습니다.");
                } catch (Exception ex) { // 애플리케이션 시작 중 발생할 수 있는 다른 예외 포착
                    ex.printStackTrace(); // 디버깅을 위해 전체 스택 트레이스 로깅
                    showAlert(AlertType.ERROR, "오류", "애플리케이션 시작 오류",
                              "ITTIA 애플리케이션을 시작할 수 없습니다: " + ex.getMessage());
                }
            });
            root.getChildren().add(button);
        }
    }

    // --- Button Action Handling ---

    /**
     * 버튼이 눌렸을 때 트리거되는 액션을 처리합니다.
     * 버튼의 텍스트에 따라 특정 메서드로 작업을 위임합니다.
     * @param buttonText 눌린 버튼의 텍스트.
     * @throws Exception 버튼 액션 실행 중 오류가 발생하면 던져집니다.
     */
    private void handleButtonPress(String buttonText) throws Exception {
        switch (buttonText) {
            case "Log In":
                System.out.println("로그인 기능 구현 예정.");
                // TODO: 실제 로그인 로직 또는 로그인 창 실행 추가
                showAlert(AlertType.INFORMATION, "정보", "로그인 기능", "로그인 기능은 아직 구현되지 않았습니다.");
                break;
            case "Prologue":
                System.out.println("프롤로그 선택됨.");
                displayResourceFile("/com/ittia/gds/txt_entry/GDSITTIA_prologue.txt", "프롤로그");
                break;
            case "Version Information":
                System.out.println("버전 1.0 - 2025년 7월."); // 속성 파일에서 동적으로 로드할 수 있음
                displayResourceFile("/com/ittia/gds/txt_entry/GDSITTIA_IDE.txt", "버전 정보");
                break;
            case "Ittia Start":
                System.out.println("ITTIA 시작 중...");
                launchGDSEMRFrame();
                break;
            case "Rescue":
                System.out.println("복구(Rescue) 액션 트리거됨. (기능 미구현)");
                // TODO: 필요한 경우 복구/복원 로직 구현
                showAlert(AlertType.INFORMATION, "정보", "복구 기능", "복구 기능은 아직 구현되지 않았습니다.");
                break;
            case "Quit":
                System.out.println("애플리케이션 종료.");
                System.exit(0); // 애플리케이션 종료
                break;
            default:
                System.err.println("인식할 수 없는 버튼 액션: " + buttonText);
                showAlert(AlertType.WARNING, "경고", "알 수 없는 액션",
                          "인식할 수 없는 액션이 요청되었습니다: " + buttonText);
                break;
        }
    }

    // --- Utility Methods ---

    /**
     * 클래스패스 내의 지정된 리소스 파일 내용을 읽고 정보 알림 대화 상자에 표시합니다.
     *
     * @param resourcePath 리소스 파일의 절대 경로 (예: "/path/to/file.txt").
     * @param title 알림 대화 상자의 제목.
     * @throws IOException 리소스 파일을 찾거나 읽을 수 없는 경우 발생.
     */
    private void displayResourceFile(String resourcePath, String title) throws IOException {
        StringBuilder content = new StringBuilder();
        try (InputStream is = getClass().getResourceAsStream(resourcePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            if (is == null) {
                // 리소스가 없는 경우 명시적으로 로깅하는 것이 좋습니다.
                System.err.println("리소스를 찾을 수 없거나 스트림이 null입니다: " + resourcePath);
                throw new IOException("파일을 찾을 수 없습니다: " + resourcePath + ". 리소스 경로와 파일 존재 여부를 확인하십시오.");
            }

            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null); // 간단한 텍스트 표시에는 헤더 텍스트 없음
            alert.setContentText(content.toString());
            alert.getDialogPane().setPrefSize(900, 800); // 가독성을 위해 선호 크기 설정
            alert.showAndWait();

        } catch (IOException e) {
            // IOException은 버튼의 액션 핸들러에서 잡히도록 다시 던집니다.
            throw e;
        } catch (Exception e) {
            // 리소스 읽기 중 발생하는 다른 예외(예: NullPointerException) 포착
            System.err.println("리소스 읽기 중 예상치 못한 오류가 발생했습니다: " + resourcePath + " - " + e.getMessage());
            throw new IOException("리소스 파일을 로드하지 못했습니다: " + resourcePath, e);
        }
    }

    /**
     * GDSEMR_frame 애플리케이션을 새로운 독립적인 스테이지에서 시작합니다.
     * 이를 통해 런처는 계속 열려 있거나 독립적으로 닫힐 수 있습니다.
     */
    private void launchGDSEMRFrame() {
       try {
          // 1) JavaFX 앱 클래스의 새 인스턴스 생성
          GDSEMR_frame emrApp = new GDSEMR_frame();

          // 2) EMR 애플리케이션을 위한 새 Stage 생성
          Stage emrStage = new Stage();

          // 3) 새 스테이지를 전달하여 start(...) 메서드를 수동으로 호출
          emrApp.start(emrStage);

          // (선택 사항) EMR을 시작한 후 런처 창을 숨기려면:
          // Stage launcherStage = (Stage) root.getScene().getWindow(); // root 또는 버튼에 대한 참조가 필요합니다.
          // launcherStage.hide();
       } catch (Exception ex) {
          ex.printStackTrace(); // 디버깅을 위해 전체 스택 트레이스 로깅
          showAlert(AlertType.ERROR, "오류", "EMR을 시작할 수 없습니다", ex.getMessage());
       }
    }

    /**
     * 알림 대화 상자를 표시하는 헬퍼 메서드.
     * @param type 알림 유형 (예: AlertType.ERROR, AlertType.INFORMATION).
     * @param title 알림 창의 제목.
     * @param header 알림의 헤더 텍스트.
     * @param content 알림의 주 내용 메시지.
     */
    private void showAlert(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }


    // --- Main Method ---

    /**
     * JavaFX 애플리케이션을 시작하는 메인 메서드.
     * @param args 명령줄 인수 (이 애플리케이션에서는 사용되지 않음).
     */
    public static void main(String[] args) {
        launch(args); // 이 메서드가 start() 메서드를 호출합니다.
    }
}