
# Translations / Language

## For Server Owners / Players

To change the language used in Essential Commands, change the `language` setting in `config/EssentialCommands.properties` to one of the supported languages:

| Language Code | Language Name       |
|---------------|---------------------|
| `de_de`         | German (Germany)    |
| `en_us`         | English (US)        |
| `es_es`         | Spanish (Spain)     |
| `fr_fr`         | French (France)     |
| `ko_kr`         | Korean (Korea)      |
| `nl_nl`         | Dutch (Netherlands) |
| `pt_br`         | Portuguese (Brazil) |
| `ru_ru`         | Russian (Russia)    |
| `zh_cn`         | Chinese (Simplified)|
| `zh_tw`         | Chinese (Traditional)|

## For Translators and Developers

The language files can be found in the [`src/main/resources/assets/essential_commands/lang`][lang-github] directory. To add a new one, simply copy the `en_us.json` file, naming it using the `[language-2-chars]_[region-2-chars].json` format shown above, then edit the file, replacing the English values with their equivalents in your desired language!

[lang-github]: https://github.com/John-Paul-R/Essential-Commands/tree/26.x/src/main/resources/assets/essential_commands/lang
