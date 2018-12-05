package ac.uk.ucl.bioreactor;

import ac.uk.ucl.bioreactor.core.Context;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Boot extends Application {
	
	@Override
	public void start(Stage stage) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/bioreactorui.fxml"));
		Context context = new Context();
		Context.setActiveContext(context);
		loader.setController(context.getUiController());
		
		BorderPane root = loader.load();
		Scene scene = new Scene(root, 1280, 720);
		
		stage.setOnCloseRequest((e) -> {
			Platform.exit();
			System.exit(0);
		});
		stage.setTitle("Bioreactor Controller");
		stage.setScene(scene);
		stage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}