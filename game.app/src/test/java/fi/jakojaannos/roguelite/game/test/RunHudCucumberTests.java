package fi.jakojaannos.roguelite.game.test;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = "pretty",
                 features = "src/test/resources/hud",
                 glue = {
                         "fi.jakojaannos.roguelite.game.test.global",
                         "fi.jakojaannos.roguelite.game.test.stepdefs.world",
                 }
                 //,tags = {"@problem"}
                 )
public class RunHudCucumberTests {
}
