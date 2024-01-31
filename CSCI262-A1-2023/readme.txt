This assignment code is written and compiled on IntelliJ IDEA Community Edition 2022.2.2
Java version: JDK 18.0.2.1

Reduction function:
1) Take in message digest (hash) and convert to long. This is done by using BigInteger, where it converts the hash into a hexadecimal variable b.
2) For the purpose of modulus calculation, the length value which represents total number of passwords is also converted to BigInteger variable l.
3) Reduction value redval is then calculated based on (b mod l). There is no need to include +1 since the lists all start from 0.