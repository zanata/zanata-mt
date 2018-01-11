var ExtractTextPlugin = require('extract-text-webpack-plugin')
var _ = require('lodash')
var stylelint = require('stylelint')
var postcssImport = require('postcss-import')
var postcssCustomProperties = require('postcss-custom-properties')
var postcssCalc = require('postcss-calc')
var postcssColorFunction = require('postcss-color-function')
var postcssCustomMedia = require('postcss-custom-media')
var postcssEsplit = require('postcss-esplit')

var postCssLoader = {
  loader: 'postcss-loader',
  options: {
    plugins: [
      postcssImport(),
      postcssCustomProperties,
      postcssCalc,
      postcssColorFunction,
      postcssCustomMedia,
      postcssEsplit({
        quiet: true
      }),
  ]}
}

module.exports = {
  entry: "./src/index.tsx",
  output: {
    filename: "bundle.js",
    path: __dirname + "/dist"
  },

  // Enable sourcemaps for debugging webpack's output.
  devtool: "source-map",

  resolve: {
    // Add '.ts' and '.tsx' as resolvable extensions.
    extensions: [".ts", ".tsx", ".js", ".json", ".less", ".css"]
  },

  module: {
    rules: [
      // All files with a '.ts' or '.tsx' extension will be handled by 'awesome-typescript-loader'.
      { test: /\.tsx?$/, loader: "awesome-typescript-loader" },

      // All output '.js' files will have any sourcemaps re-processed by 'source-map-loader'.
      { enforce: "pre", test: /\.js$/, loader: "source-map-loader" },
      {
        test: /\.css$/,
        use: ExtractTextPlugin.extract({
          fallback: 'style-loader',
          use: [
            {
              loader: 'css-loader'
            },
            'csso-loader'
          ]
        })
      },

      /* Bundles bootstrap css into the same bundle as the other css.
       * TODO look at running through csso, same as other css
       */
      {
        test: /\.less$/,
        exclude: /node_modules/,
        use: ExtractTextPlugin.extract({
          fallback: 'style-loader',
          use: [
            {
              loader: 'postcss-loader',
              options: {
                plugins: [
                  require("postcss-import"),
                  require("stylelint"),
                  require("postcss-cssnext")({warnForDuplicates: false}),
                  require("postcss-reporter"),
                  require("cssnano")
                ]
              }
            }, postCssLoader,
            'less-loader'
          ]
        })
      }
    ]
},

  plugins: _.compact([
    /* Outputs css to a separate file per entry-point.
       Note the call to .extract above */
    new ExtractTextPlugin({
      filename: '[name].css',
      // storybook should use the fallback: style-loader
    })
  ]),

  // When importing a module whose path matches one of the following, just
  // assume a corresponding global variable exists and use that instead.
  // This is important because it allows us to avoid bundling all of our
  // dependencies, which allows browsers to cache those libraries between builds.
  externals: {
    "react": "React",
    "react-dom": "ReactDOM"
  }
};
