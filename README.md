# C-- 
C-- is an interpreted programming language made in java. Basically c with less features and some syntax taken/inspired from python.

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
#### MATHEMATICS
supports addition ('+'), substraction ('-'), multiplication ('*'), and division ('/')
```python
int: num = 10 + 5;
int: num = 10 - 5;
int: num = 10 * 5;
int: num = 10 / 5;
```

#### COMPARISON and EQUALITY
Comparison and equality gives a boolean value (true or false). C-- supports equals ('=='), not equals ('!='), less than ('<'), less than or equal to ('<='), more than ('>'), more than or equal to ('>=')
```python
bool: result1 = 12 == 10; # false
bool: result2 = 5 == 5; # true
bool: result3 = 5 < 7; # true
```

#### DATATYPES, VARIABLE DECLARATION, and ASSIGNMENT
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

# input always outputs a str
echo("Input your text here: ");
str: inputted_text = input();
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
```python
float: decimal_number = 10.5;
int: whole_number = (int) decimal_number;

# typecasting a str into a number
str: str_num = "10.15";
double: num = (double) str_num;
```
