package fi.jakojaannos.roguelite.launcher.arguments;

import fi.jakojaannos.roguelite.launcher.arguments.parameters.Parameter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ArgumentsTest {
    @Test
    void passingNonPrefixedStringAsArgumentThrows() {
        assertThrows(ArgumentParsingException.class,
                     () -> Arguments.builder()
                                    .consume("thisIsNotAValidArgument"));
    }

    @Test
    void singleCharArgumentsArePrefixedWithOneHyphen() throws ArgumentParsingException {
        Argument.Action mockAction = mock(Argument.Action.class);
        Arguments.builder()
                 .with(Argument.withName("a")
                               .withAction(mockAction))
                 .consume("-a");

        verify(mockAction).perform(any());
    }

    @Test
    void multipleCharArgumentsArePrefixedWithTwoHyphens() throws ArgumentParsingException {
        Argument.Action mockAction = mock(Argument.Action.class);
        Arguments.builder()
                 .with(Argument.withName("test")
                               .withAction(mockAction))
                 .consume("--test");

        verify(mockAction).perform(any());
    }

    @Test
    void passingEmptyStringThrows() {
        assertThrows(ArgumentParsingException.class,
                     () -> Arguments.builder()
                                    .consume(""));
    }

    @Test
    void passingOnlySingleHyphenThrows() {
        assertThrows(ArgumentParsingException.class,
                     () -> Arguments.builder()
                                    .consume("-"));
    }

    @Test
    void passingOnlyTwoHyphensThrows() {
        assertThrows(ArgumentParsingException.class,
                     () -> Arguments.builder()
                                    .consume("--"));
    }

    @Test
    void unknownArgumentsAreIgnoredWhenIgnoreUnknownIsSet() {
        assertDoesNotThrow(
                () -> Arguments.builder()
                               .ignoreUnknown()
                               .consume("--thisIsAnUnknownArgument"));
    }

    @Test
    void unknownArgumentsThrowWhenIgnoreUnknownIsNotSet() {
        assertThrows(
                ArgumentParsingException.class,
                () -> Arguments.builder()
                               .consume("--thisIsAnUnknownArgument"));
    }

    @Test
    void argumentsWithParamsArePassedTheCorrectParameters_singleArgument_singleParameter() throws ArgumentParsingException {
        Parameter mockParam = mock(Parameter.class);
        Arguments.builder()
                 .with(Argument.withName("test")
                               .withAction(params -> params.parameter(mockParam)))
                 .consume("--test", "parameter");

        verify(mockParam).parse("parameter");
    }

    @Test
    void argumentsWithParamsArePassedTheCorrectParameters_singleArgument_multipleParameters() throws ArgumentParsingException {
        Parameter mockParam = mock(Parameter.class);
        Arguments.builder()
                 .with(Argument.withName("test")
                               .withAction(params -> {
                                   params.parameter(mockParam);
                                   params.parameter(mockParam);
                                   params.parameter(mockParam);
                                   params.parameter(mockParam);
                               }))
                 .consume("--test", "param1", "param2", "param3", "doesThisWerk");

        verify(mockParam).parse("param1");
        verify(mockParam).parse("param2");
        verify(mockParam).parse("param3");
        verify(mockParam).parse("doesThisWerk");
    }

    @Test
    void argumentsWithParamsArePassedTheCorrectParameters_multipleArgument_multipleParameters() throws ArgumentParsingException {
        Parameter mockParamA = mock(Parameter.class);
        Parameter mockParamB = mock(Parameter.class);
        Arguments.builder()
                 .with(Argument.withName("test")
                               .withAction(params -> {
                                   params.parameter(mockParamA);
                                   params.parameter(mockParamA);
                                   params.parameter(mockParamA);
                                   params.parameter(mockParamA);
                               }))
                 .with(Argument.withName("a")
                               .withAction(params -> {
                                   params.parameter(mockParamB);
                                   params.parameter(mockParamB);
                                   params.parameter(mockParamB);
                                   params.parameter(mockParamB);
                               }))
                 .consume("--test", "param1", "param2", "param3", "doesThisWerk",
                          "-a", "abc", "def", "123", "!\"#");

        verify(mockParamA).parse("param1");
        verify(mockParamA).parse("param2");
        verify(mockParamA).parse("param3");
        verify(mockParamA).parse("doesThisWerk");

        verify(mockParamB).parse("abc");
        verify(mockParamB).parse("def");
        verify(mockParamB).parse("123");
        verify(mockParamB).parse("!\"#");
    }

    @Test
    void passingExtraParameterToAnArgumentThrows() {
        Parameter mockParam = mock(Parameter.class);
        assertThrows(ArgumentParsingException.class,
                     () -> Arguments.builder()
                                    .with(Argument.withName("test")
                                                  .withAction(params -> params.parameter(mockParam)))
                                    .consume("--test", "parameter", "oneTooMuch"));
    }

    @Test
    void passingTooFewParametersToAnArgumentThrows() {
        Parameter mockParam = mock(Parameter.class);
        assertThrows(ArgumentParsingException.class,
                     () -> Arguments.builder()
                                    .with(Argument.withName("test")
                                                  .withAction(params -> {
                                                      params.parameter(mockParam);
                                                      params.parameter(mockParam);
                                                  }))
                                    .consume("--test", "oneTooFew"));
    }

    @Test
    void passingNoParametersToAnArgumentWithParametersThrows() {
        Parameter mockParam = mock(Parameter.class);
        assertThrows(ArgumentParsingException.class,
                     () -> Arguments.builder()
                                    .with(Argument.withName("test")
                                                  .withAction(params -> params.parameter(mockParam)))
                                    .consume("--test"));
    }
}
