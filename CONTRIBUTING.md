Code Conventions
----------------

*All Code in a codebase should be written so that you are not able just by looking at the code to tell which of the contributors has written it. Always follow the conventions, they make the code look clean. Clean code equals readable code, readable code equals maintainable code.*


### Use `Optional` over `null`
Using `null` is bad. Public methods should never return `null` and should return `Optional<T>` instead. Only exceptions to this are performance-critical areas where wrapping to `Optional` could cause unnecessary allocations and thus possibly significant overhead. However, API can usually be designed to use optionals, hiding any nullness behind the public-facing API.

*TL;DR: Genrally speaking, any time you return `null` from any `public` method, a kitten dies.*


### Naming Conventions
Avoid abbreviations. As of writing this, the year is soon 2020, we have autocomplete and modern editors/IDEs. There is no sensible reason to write stuff like `val em` over `val entityManager`. Short abbreaviations only obfuscate the code, making it harder to read. Use full words for variable names. Yes, this includes commonly used abbreviations like `damage -> dmg`. Don't shorten those either, you are not that lazy. Use autocomplete if you are.

In general, class names start with a capital letter and instances should have similar name, but starting with lowercase. Moreover, component instances should always have the name of the component class in their name.

Examples:
```java
EntityManager entityManager;    // NOT 'em'
TileType tileType;              // NOT 'tt' or 'tType'
Health health;                  // NOT 'hp'
DamageInstance damageInstance;  // NOT 'dmg'
```

Also:
```java
Collider otherCollider = ... // OK.
Collider otherBounds = ...   // NO. Don't name things as something they are not.
```
The name `otherCollider` immediately tells the one reading the code that the instance is a collider component, belonging to an entity tagged as `other`. On the other hand `otherBounds` misleads the reader to think that the instance has boundaries of some kind. While partially true, this might cause confusion and bugs later down the line.

There are some situations where it is allowed to use names breaking this rule. These are both valid *(assuming the loop/method continues for only 4-8 lines at most afterwards)*:
```java
for (val damageInstance : damageInstances) { /* some short 4-8 line loop */ }
for (val instance : damageInstances) { /* some short 4-8 line loop */ }
```
And same goes for these:
```java
val otherEntity = collision.getOtherEntity();
val other = collision.getOtherEntity();
```

The more verbose form is always preferred, but in short loops or methods it does not matter that much whether or not the full noun is postfixed.

*TL;DR:*
- All names should be words. Not abbreviations.
- All names, except constants, are `CamelCase` or `camelCase`
    - Class/interface names start with capitall letter. e.g. `class GameState`
    - Method names start with lowercase letter. e.g. `tick(...)` or `getComponentOf(...)`
    - Variable/field/parameter names start with lowercase letters. e.g. `double delta`
- Constants are `SNAKE_CASE_WITH_ALLCAPS`
- **DO NOT** add a postfix to Component classes. e.g. `class Health`
- **DO NOT** add a postfix to Resource classes. e.g. `class Collisions`
- System classes **should** be postfixed with `System`. e.g. `class ApplyVelocitySystem`


### Declaring multiple fields on a single line

It is fine to declare multiple fields of same type on one line if they are closely related. For example, these are OK
```java
private final double x, y;
private final double width, height;
private final long someTimestamp, someDuration;
```

Do **not** do stuff like
```java
private final double x, length, velocityZ, someDuration, howOldIsMoominpappa;
```

Also, even if things are closely related, if one of the fields requires JDoc comments or documentation, 
it most likely deserves its own line, too.
```java
// Do this:
public double maxHealth;

/** Some documentation */
public double currentHealth;


// NOT this:
public double maxHealth,
    /** Some documentation */
    currentHealth;
```
