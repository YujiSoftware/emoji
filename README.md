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
    /* font-size: 50px; */
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
void main() {
  println(Character.isEmoji('🤧'));
}
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

# おさらい：Unicode とは？

- 全世界のあらゆる文字を含む文字集合
- 文字に **コードポイント（符号位置）** を割り当てている
- 範囲は、U+0000 ～ U+10FFFF （21ビット）
  - U+0000 - U+FFFF までを**基本多言語面**という
    - 例：あ= U+3042
  - U+10000 - U+10FFFF を**追加面**という
    - 例：🤧 = U+1F927
- 絵文字の多くは追加面にある
---

# おさらい：UTF-16 とは？

- Unicode を **16bit のコードユニット**で扱う
  - Java では、コードユニット = char 型
  - 例：あ = \u3042
- 追加面にあるコードポイントは、2つのコードユニットを使って扱う
  - 例：🤧 = \uD83E \uDD27
  - この2つセットのことを**サロゲートペア**という

---

# 冒頭のコードの挙動

```java
Character.isEmoji('🤧');
↓
Character.isEmoji(\uD83E\uDD27);
```

- コンパイラが文字リテラルをコードユニットに置き換える
- この絵文字の場合、**2つの**コードユニットになってしまうので、言語仕様違反によりコンパイルエラーになる
  > 文字リテラルはUTF-16コードユニットのみを表すことができ、つまり\u0000から\uffffまでの値に限定されます。


---

# 正しい処理

- isEmoji の引数に**コードポイントを指定する**

```java
Character.isEmoji(0x1F927)
```

- もしくは、文字列からコードポイントを取得する

```java
Character.isEmoji("🤧".charCodeAt(0))
```


---

# ありがちな間違い

```java
Character.isEmoji("🤧".charAt(0))
```

- **コードユニット**を取得している
  - 上位サロゲートのみになる（`\uD83E`）
  - 表示すると無効な文字（いわゆる豆腐）になる
- この値は絵文字ではないので false を返す

<hr>

- コンパイラですら間違っていたぐらいありがちなミス

---

# 間違えていても動くこともある

```java
Character.isEmoji('✌')
```

- ✌ = U+270C = \u270C
- コードポイントもコードユニットも同じ
  - つまり、codePointAt でも charAt でも同じように動く

<hr>

- 入力文字によって、バグったりバグらなかったりという微妙な挙動になるので要注意

---

# 絵文字に限らない

- 絵文字以外でもサロゲートペアはある
  - 例：𩸽(ほっけ) = U+29E3D = \uD867 \uDE3D
- 英数字以外を扱う際は、必ずコードポイント単位で扱うのが安全

---



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
  - [JDK-8354908: javac mishandles supplementary character in character literal](https://bugs.openjdk.org/browse/JDK-8354908)
    - 当初のチケット名は
     "Character.isEmoji(int) returns incorrect results"
    - つまり、API のバグだと思われていたw
