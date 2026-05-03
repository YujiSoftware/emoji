---
marp: true
author: "@YujiSoftware"
title: "Javaで絵文字を正しく扱おう🥲"
description: "JJUG CCC 2026 Spring のセッション資料です。"
image: "img/card.png"
theme: gaia
style: |
  :root {
    --color-background: #fff;
    --color-foreground: #000;
    --color-highlight: #1a0dab;
    --color-dimmed: #888;
    font-size: 40px;
    line-height: 1.2;
  }

  section.lead img {
    border-radius: 50%;
    vertical-align: middle;
  }
  
  img[alt~="center"] {
    display: block;
    margin: 0 auto;
  }

  .info {
    border: 2px solid #dee2e6;
    background-color: #cff4fc;
    color: #052c65;
    padding: 0.5em;
    border-radius: 10px;
    margin: 10px 0;
  }
  .info::before {
    content: "📝";
  }

  section.puzzlers li {
    font-size: 50px;
  }
---

<!-- _class: lead -->

# Javaで絵文字を正しく扱おう🥲
<br>

![width:150px height:150px](img/photo.jpg) @YujiSoftware

---

<!-- _class: puzzlers -->

# Java Puzzlers

このコードを実行した結果は？
```java
Character.isEmoji('🤧');
```

1. true
2. false
3. コンパイルエラー
4. 実行時エラー

---

# 正解

3. コンパイルエラー

```java
IsEmoji.java:3: エラー: 文字リテラルに複数のUTF-16コード・ユニットが含まれています
        Character.isEmoji('🤧');
                           ^
```

- 1文字なのに複数？？？

---

# バージョンによって挙動が異なる

- Java 24 までは **コンパイルできていた**
  - 戻り値は **false**

```java
jshell> Character.isEmoji('🤧')
$1 ==> false
```

---

# 絵文字によって結果が異なる

- もちろん、true になる場合もある

```java
jshell> Character.isEmoji('☺')
$1 ==> true

jshell> Character.isEmoji('✌')
$2 ==> true
```

---

<!-- _class: lead -->

# キーワード
# 「サロゲートペア」

---

# サロゲートペアとは？


---

<!-- _class: puzzlers -->

# Java Puzzlers

Character.isEmoji(…) で true になるのは？

1. \# （シャープ）
2. ? （はてな）
3. ※ （米印）
4. ★ （スター）

---

<!-- _class: lead -->

# キーワード
# 「文字シーケンス」

---

<!-- _class: puzzlers -->

# Java Puzzlers

Java 25 と 26 で結果が異なるのは？

1. 🙂‍↔️ （首を横に振る）
2. ❤️‍🩹 （包帯を巻いたハート）
3. 🧙 （魔法使い）
4. 🪎 （宝箱）

---

<!-- _class: lead -->

# キーワード
# 「Unicdoe バージョン」

---

# 参考資料

- Java 25 で修正されたバグ
 * [JDK-8354908: javac mishandles supplementary character in character literal](https://bugs.openjdk.org/browse/JDK-8354908)
