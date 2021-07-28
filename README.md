# MaskedEt

Custom view that handles masked input with any custom mask.

## Usage:

To initialize mask call setFormattingSettings method:

```kotlin
maskedEt.setFormattingSettings(
    "+7 (000) 000-00-00", //that's a mask
    mapOf(  // that's an explanation of every char in mask
        '0' to "1234567890",
        '+' to MaskedEt.PATTERN_VALUE,
        '7' to MaskedEt.PATTERN_VALUE,
        '(' to MaskedEt.PATTERN_VALUE,
        ')' to MaskedEt.PATTERN_VALUE,
        '-' to MaskedEt.PATTERN_VALUE
    ),
    true
)
```

With this settings user can type only symbols from "1234567890" on positions of mask with '0' and nothing else. 
Other mask symbols marked with ```MaskedEt.PATTERN_VALUE``` are service symbols that appear automatically when cursor reaches their mask position.

To reset formatting algorythm call method ```resetFormatting```.

To get raw text without service symbols call method ```getRawText```.

