const path = require("path");
const CopyWebpackPlugin = require("copy-webpack-plugin");

config.resolve.fallback = {
  ...config.resolve.fallback,
  fs: false,
  path: false,
  crypto: false,
};

config.plugins.push(
  new CopyWebpackPlugin({
    patterns: [
      {
        from: path.join(path.dirname(require.resolve("sql.js")), "sql-wasm.wasm"),
        to: "sql-wasm.wasm",
      },
    ],
  }),
);
