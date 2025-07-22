module com.ittia.gds {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
//	requires org.slf4j;
	requires java.desktop; // 그래픽 기능에 필요할 수 있음
    opens com.ittia.gds to javafx.fxml, javafx.graphics;
}