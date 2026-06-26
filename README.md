# C--
this is c with less features and some syntax taken/inspired from python.

## FEATURES
- mathematics
- comparison and equality
- variable declaration
- variable assignment
- primitive datatypes (void, boolean, short, int, long, float, double, char, str)
- string concatenation
- formatted string
- comments
- echo
- input
- if-else
- ternary
- while loop
- for loop
- typecasting

## SYNTAX
#### VARIABLE DECLARATION and DATATYPES
```python
bool: boolean = true;
int: integer_num = 1;
short: short_num = 1;
long: long_num = 1;
float: float_num = 0.5;
double: double_num = 0.5;
str: string = "this is a text";
```

#### CONCATENATING STRINGS and FORMATTED STRINGS
```python
int: i = 0;
str: text = "string concatenation: " + i;
text = text + "\nstring formatting: {i}";
text = "{text}\nlogic inside str fields: {i * 12 / 5}"; 
```

#### COMMENTS
Text inside a comment will not be executed. Comments starts from '#' and everything proceeding it will not be processed by the language until it changes lines.
```python
# this is a comment
int: i = 0; # inline comments are also possible
```

#### ECHO and INPUT
```python
# echo writes onto the terminal
echo("a text");   # accepts string. result      : 'a text'
echo(12);         # accepts all numbers. result : '12'
echo(true);       # accepts bool. result        : 'true'
echo();           # invalid! echo cannot be empty.
```

#### IF-ELSE
```python
if (12 == 12) {
    echo("result is true");
}

if (10 == 12) {
    echo("this would not be executed.");
} else {
    echo("else block executed.");
}

int: i = 10;
if (i == 12) {
    echo("this if block is never executed.");
} else if (i == 10) {
    echo("this else if block is executed.");
    i = i + 2;
} else {
    echo("as a branch has already ran, everything else is skipped.");
}
echo(i); # echoes 12
```

#### TERNARY
syntax:
[boolean] ? [then-branch] : [else-branch];
```python
int: number = 0;
number = number == 2 ? 1 : 2;
```

#### WHILE LOOP
```python
# the variable 'i' will be printed 10 times (1, 2, 3, ..., 9, 10).
int: i = 1;
while (i <= 10) {
    echo("{i}\n");
    i += 1;
}
```

#### FOR LOOP 
The code below executes the same way as the previous while loop code. Inside the parenthesis of the for loop, there are 3 subsections:
1. initialization: declaring variables
2. condition: the condition which determines if the loop will continue running
3. iterator: modifying a variable by a set patttern
```python
for (int: i = 1; i <= 10; i += 1) {
    echo("{i}\n");
}
```

#### TYPECASTING
Typecasting changes the type of a value into another.
