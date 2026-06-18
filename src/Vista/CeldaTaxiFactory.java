package Vista;

import javafx.scene.control.ListCell;
import javafx.css.PseudoClass;

/**
 * Fábrica de celdas para la ListView de taxis.
 * Separa la lógica visual del Controlador principal.
 */
public class CeldaTaxiFactory extends ListCell<String> {
    
    // El pseudo-estado "pressed" es el que activa el CSS hundido retro que hiciste
    private final PseudoClass pressedClass = PseudoClass.getPseudoClass("pressed");

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            getStyleClass().remove("button");
            pseudoClassStateChanged(pressedClass, false);
        } else {
            setText(item);
            // Le damos el estilo de botón Verde Oliva
            if (!getStyleClass().contains("button")) {
                getStyleClass().add("button");
            }
            // Si dice "OCUPADO", forzamos el pseudo-estado para que se vea hundido
            pseudoClassStateChanged(pressedClass, item.contains("OCUPADO"));
        }
        
        // Evitamos que el usuario haga clic real en la celda
        setMouseTransparent(true);
        setFocusTraversable(false);
    }
}