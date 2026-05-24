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

  span.emphasis {
    color: red;
    font-weight: bold;
  }
  span.underline {
    text-decoration: underline;
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
  IO.println(Character.isEmoji('🤧'));
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
  - 戻り値は、なぜか **false**

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
- 絵文字の多くは追加面に配置されている
---

# おさらい：UTF-16 とは？

- Unicode を **16bit のコードユニット（符号単位）** で扱う
  - Java では、コードユニット = char 型
  - 例：あ = \u3042
- 追加面に配置されている文字は、2つのコードユニットを使って扱う
  - 例：🤧 = \uD83E \uDD27
  - この2つセットのことを**サロゲートペア**という
    - それぞれ**上位**サロゲート・**下位**サロゲートという

---

# 冒頭のコードの挙動

```java
Character.isEmoji('🤧');
                   ↓
Character.isEmoji(\uD83E\uDD27);
```

- コンパイラが文字リテラルをコードユニットに置き換える
- この絵文字の場合、**2つの**コードユニットになってしまう
  → 言語仕様違反によりコンパイルエラーになる
  > 文字リテラルはUTF-16コードユニットのみを表すことができ、つまり\u0000から\uffffまでの値に限定されます。（JLS 3.10.4. Character Literals より）


---

# 正しい処理

- isEmoji の引数に**コードポイントを直接指定する**

```java
Character.isEmoji(0x1F927)
```

- 文字列からコードポイントを取得して指定する

```java
Character.isEmoji("🤧".codePointAt(0))
```

---

# ありがちな間違い

```java
Character.isEmoji("🤧".charAt(0))
```

- **コードユニット**を取得している
  - 上位サロゲートのみになる（`\uD83E`）
    - これは表示すると &#xD83E; （壊れた文字）になる
  - 絵文字ではないので、**isEmoji が false となる**

---

# サロゲートペアではない絵文字

```java
Character.isEmoji('✌')
```

- ✌ = U+270C = \u270C
- コードポイントもコードユニットも同じ
  - codePointAt でも charAt でも同じ結果になる

<hr>

- charAt でも絵文字によっては正しく動くという微妙な挙動になるので要注意

---

# 補足：絵文字に限らない

- 絵文字以外でもサロゲートペアは使われている
- 英数字以外を含む場合は、**必ず**コードポイント単位で扱うのが安全

|文字|(よみ)|コードポイント|コードユニット|
|--|--|--|--|
|𩸽|ほっけ|U+29E3D|\uD867 \uDE3D|
|𩹉|トビウオ|U+29E49|\uD867 \uDE49|
|🄐|カッコエー|U+1F100|\uD83C \uDD10|
---

<!-- _class: puzzlers -->

# Java Puzzlers

Character.isEmoji(…) で true になる文字は？

| | 文字 | 説明 | コードポイント |
|------|:--:|:--:|:--:|
| 1 | 0 | (数字の)ゼロ | U+0030 |
| 2 | ? | はてな | U+003F |
| 3 | ※ | 米印 | U+203B |
| 4 |  ♫ | 音符 | U+266B |

---

# Java Puzzlers

Character.isEmoji(…) で true になる文字は？

| | 文字 | 説明 | コードポイント | isEmoji |
|------|:--:|:--:|:--:|:---:|
| 1 | 0 | (数字の)ゼロ | U+0030 | <span class="emphasis">true</span> |
| 2 | ? | はてな | U+003F | false |
| 3 | ※ | 米印 | U+203B | false |
| 4 |  ♫ | 音符 | U+266B | false |

---

# なぜ？

- **Unicode でそう定義されているから**
  - [emoji-data.txt](https://www.unicode.org/Public/17.0.0/ucd/emoji/emoji-data.txt) という定義ファイルの Emoji プロパティ一覧に 0（U+0030）が含まれている
- Character.isEmoji(int codePoint) はこの定義に従って戻り値を決めている

```
0023        ; Emoji   # E0.0   [1] (#️)       hash sign
002A        ; Emoji   # E0.0   [1] (*️)       asterisk
0030..0039  ; Emoji   # E0.0  [10] (0️..9️)    digit zero..digit nine
00A9        ; Emoji   # E0.6   [1] (©️)       copyright
00AE        ; Emoji   # E0.6   [1] (®️)       registered
```
---

# なぜ 0 は Emoji と定義？

- **絵文字シーケンス**により、絵文字となるから
  - ↑ 複数のコードポイントを組み合わせて、1つの絵文字とする仕組み
- 例：0️⃣ という絵文字
  - **U+0030**, U+FE0F, U+20E3 という3つのコードポイントの並びで構成されている
- つまり、1文字 &#x2260; 1コードポイント
  - 1文字のことを書記素クラスターという

---

# 0️⃣ のコードポイント

- U+0030
  - 数字のゼロ 0
- U+FE0F
  - 異体字セレクター16（Variable Selector 16）
  - 明示的に絵文字で表示するという指定
- U+20E3
  - キーを表す合成文字（□） 

---

# Q. なぜこんな設計？

- A. **互換性のため**

<hr>

- 0️⃣ に単独のコードポイントを割り当てた場合
  - 対応していない環境では □（通称：豆腐）で表示される
    - 何も伝わらない
- 絵文字シーケンスの場合
  - 対応していない環境では 0 □ と表示される
    - 最低限の内容は伝わる

---

# 絵文字の傾向

- 最近追加された絵文字の**多く**が絵文字シーケンスで構成されている
  - [Unicode Emoji v17.0](https://unicode.org/emoji/charts/index.html)
- 例：
  - ❤️‍🔥 = ❤ 🔥（ U+2764 U+FE0F U+200D U+1F525 ）
  - ❤️‍🩹 = ❤ 🩹（ U+2764 U+FE0F U+200D U+1F525 ）
  - 🍋‍🟩 = 🍋 🟩（ U+1F34B U+200D U+1F7E9 ）
    - ライム = レモン + 緑色の四角
---

# シーケンス単位で扱うには？

- シーケンスをひとかたまりで処理をする必要がある
- Java 20 以降なら、**BreakIterator.getCharacterInstance()** で扱える
  - [\[JDK-8291660\] Grapheme support in BreakIterator - Java Bug System](https://bugs.openjdk.org/browse/JDK-8291660)
  - それ以前のバージョンなら、ICU4J の BreakIterator を使う

---

# サンプルコード

```java
public static List<String> deconstruct(String text) {        
        BreakIterator it = BreakIterator.getCharacterInstance();
        it.setText(text);

        List<String> clusters = new ArrayList<>();
        int prev = 0;
        while (it.next() != BreakIterator.DONE) {
            clusters.add(text.substring(prev, it.current()));
            prev = it.current();
        }
        return clusters;
    }
```

---

# 参考資料

- Java 25 で修正されたバグ
  - [JDK-8354908: javac mishandles supplementary character in character literal](https://bugs.openjdk.org/browse/JDK-8354908)
    - 当初のチケット名は
     "Character.isEmoji(int) returns incorrect results"
    - つまり、API のバグだと思われていた

---

# メモ

[Unicode 絵文字にまつわるあれこれ (絵文字の標準とプログラム上でのハンドリング) #Ruby - Qiita](https://qiita.com/_sobataro/items/47989ee4b573e0c2adfc)
```
emoji
  ├ singleton (単体のコードポイントからなる絵文字)
  └ emoji sequence (複数のコードポイントからなる絵文字)
      ├ emoji core sequence (通常の絵文字)
      │  ├ emoji combining sequence (囲み文字)
      │  ├ emoji modifier sequence (skin tone 絵文字)
      │  └ emoji flag sequence (国旗絵文字)
      ├ emoji zwj sequence (家族絵文字、職業絵文字など)
      └ emoji tag sequence (タグ絵文字)
```
