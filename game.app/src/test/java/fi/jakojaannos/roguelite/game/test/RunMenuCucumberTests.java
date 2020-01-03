package fi.jakojaannos.roguelite.game.test;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = "pretty",
                 features = "src/test/resources/menus",
                 glue = {
                         "fi.jakojaannos.roguelite.game.test.global",
                         "fi.jakojaannos.roguelite.game.test.stepdefs.render",
                         "fi.jakojaannos.roguelite.game.test.stepdefs.world",
                         "fi.jakojaannos.roguelite.game.test.stepdefs.simulation",
                 }
                 //,tags = {"@problem"}
                 )
public class RunMenuCucumberTests {
}
