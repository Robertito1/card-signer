# Local Birthday Card & Message Signer

An offline, console-based greeting card utility. Create a card from a template,
enter the recipient's details, collect signatures and wishes from as many people
as you like, preview the finished card as an ASCII-bordered design, and export it
to a `.txt` file. Cards are saved to disk, so they persist between runs.

## Features

- Choose a card template (Classic Birthday, Balloon Party, or Elegant) and enter
  recipient details: name, turning age, and occasion.
- Keep cards persistent on disk; each card is stored as its own file.
- Add signatures (signer name and wish) to any card, as many as you need.
- Edit or remove individual signatures on a card.
- Preview a card as formatted, ASCII-bordered text.
- Export a card to a `.txt` file in the `exports` folder.
- Delete a card, with a confirmation prompt.
- Input validation with re-prompting on bad input (names, ages, choices, text length).

## Project Structure

```
src/
  main/java/com/bptn/project/
    App.java                 Entry point; wires the components together
    exception/               ValidationException
    export/                  CardExporter (writes rendered cards to .txt)
    io/                      InputProvider interface and ConsoleInput implementation
    model/                   Card, Signature
    model/template/          CardTemplate and the three template styles, TemplateCatalog
    render/                  CardRenderer interface and AsciiCardRenderer
    repository/              CardRepository interface, FileCardRepository, InMemoryCardRepository
    ui/                      Menu (console menu and workflows)
    util/                    InputValidator (all validation rules)
data/cards/                  Saved card files (created at runtime)
exports/                     Exported .txt cards (created at runtime)
```

The entry point is `com.bptn.project.App`, which starts the console menu.

## Prerequisites

- Java 21 or later (the project is compiled with `maven.compiler.source` and
  `maven.compiler.target` set to 21)
- Maven, or an IDE with Maven support such as Eclipse

No external services or API keys are required.

## Setup

1. **Clone the repository**

   ```bash
   git clone <repo-url>
   ```

2. **Import the project**

   In Eclipse, choose **File → Import**, select **Existing Maven Projects**, and
   point to the cloned folder. Or, from the command line, build it with:

   ```bash
   mvn compile
   ```

## Running the application

From an IDE, right-click `App.java` in the `com.bptn.project` package and choose
**Run As → Java Application**.

From the command line, after compiling:

```bash
java -cp target/classes com.bptn.project.App
```

The application runs in the terminal and shows the main menu:

1. Create a new card
2. List cards
3. Preview a card
4. Sign a card
5. Edit a signature
6. Remove a signature
7. Delete a card
8. Export a card to .txt
9. Exit

Cards are saved to `data/cards/` and exports are written to `exports/`, both
relative to the directory the application is started from.

## Running tests

There are currently no unit tests in the project (there is no `src/test`
directory). The pom includes JUnit 5 dependencies, so tests can be added under
`src/test/java` and run in the IDE with **Run As → JUnit Test**, or with
`mvn test`.

## Notes

- Card files use a simple line-based text format (`id=`, `template=`,
  `recipient=`, `occasion=`, `age=`, `sig=`). Special characters are escaped.
- The age is only saved for cards whose occasion contains the word "birthday".
- The data and export folders are created automatically the first time they are needed.
- The pom declares a PostgreSQL driver dependency, but the application does not
  use a database. Data is stored in files only.

## Contributing

Pull requests are welcome! For major changes, please open an issue first to
discuss what you would like to change.

## License

This project is provided for educational purposes and does not include an
explicit license.
