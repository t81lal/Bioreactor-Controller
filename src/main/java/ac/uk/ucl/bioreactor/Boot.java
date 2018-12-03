package ac.uk.ucl.bioreactor;

import ac.uk.ucl.bioreactor.core.Reactor;
import ac.uk.ucl.bioreactor.ui.UIController;
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
		Reactor reactor = _getReactorImpl();
		if(reactor == null) {
			System.err.println("wtf");
			return;
		}
		UIController controller = new UIController(reactor);
		loader.setController(controller);
		
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
	
	private static Reactor _getReactorImpl() {
		return new MockReactor();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}