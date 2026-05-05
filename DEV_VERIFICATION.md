# RegenResources — 再生経路の検証手順

`config/RegenResources/regen-resources-common.toml` の **`Integration.diagnosticTraceLog`** を `true` にすると、`latest.log` に `[RegenDiag]` が出ます。調査が終わったら **`false` に戻す**こと。

**プリセットの `delayTicks` はゲームティック数**です（バニラはおおよそ **20 tick ≒ 1 秒**）。例: `200` ≒ 10 秒、`12000` ≒ **10 分**。長くしすぎると、ログアウトが復元より先になり `regen_process run` がそのセッションのログに出ません。

## 最低限の一通り（シングル統合）

1. プリセット対象の鉱石をサバイバルで破壊する。
2. **`delayTicks`（例: 200）相当の時間（約 10 秒）待つ。** シェル直後にログアウトすると、`regen_process run` はまだ出ないのが普通。
3. `latest.log` で次の順を確認する（同一座標でよい）:
   - `commitOreBreakRegen queued TT + task`
   - `shell placed` → `syncFromWorldStorage`（begin → flush immediate → flush deferred）
   - 待機後に `regen_process run` → `restored ore`

## 再接続／ログアウトが絡む不具合を疑うとき

- シェルが出たあと、**復元ログが出るまで**ワールドにいるか、ログアウト後に再入室してから時間を進める。
- **統合サーバー**ではクライアントのログに加え、**サーバー側のログ**も見る。
- **再生時間が短い（例: 10 秒未満）**だと、ログアウトが復元より先になり `[RegenDiag]` に `regen_process` が載らないことがあります。また「再生シェルだけワールドにある」状態が短く、不具合を取りこぼしやすいです。**`RegenPresets` の該当エントリで `delayTicks` を 400 以上（約 20 秒以上）**などに上げてから、同じ手順で再計測するとよいです。

## ログの読み分け（概要）

| 症状の伏線 | `[RegenDiag]` で見る行 |
|------------|-------------------------|
| シェルまで | `shell placed` と `syncFromWorldStorage` の 3 行 |
| 復元まで | `regen_process run` のあと `restored ore` |
| TT／チケットずれ | `ticket mismatch` |
| ブロックが別物に | `not regen_block` / `shell skip: not air` |
| サーバー起動直後だけ遅延 | `regen_process deferred (warmup)`（ティックごとに最大 1 行） |

## ウォームアップ

`regenProcessServerWarmupTicks` が 0 より大きいとき、サーバー起動後しばらく `regen_process` が先送りされます。切り分けでは **待つ**か、一時的に **0** にして比較してください。

## スポーン読み込み％で止まり、ウィンドウの×も効かないように見えるとき

統合シングルではサーバーが固まるとクライアント全体が無応答に見えます。**CPU を食うループだけでなく、チャンク準備中のデッドロック**もあり得ます。再現したワールドで入場できない場合は、ビルド更新後に同じ手順でもう一度試してください（`RegenBlockEntity#setLevel` の TT 読み込みをサーバータスクに遅延する修正あり）。
