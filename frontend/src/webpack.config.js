const Webpack = require('webpack');
const Path = require('path');
const _ = require('lodash')
const UglifyJsPlugin = require('uglifyjs-webpack-plugin')
// const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin
// const OptimizeCSSAssetsPlugin = require('optimize-css-assets-webpack-plugin')
const HtmlWebpackPlugin = require('html-webpack-plugin');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const ManifestPlugin = require('webpack-manifest-plugin')

const index = process.argv.indexOf('--mode')
const isProd = index >= 0 ? process.argv[index + 1] === 'production' :  false
const outPath = Path.join(__dirname, './dist');
const sourcePath = Path.join(__dirname, './');

var postCssLoader = {
  loader: 'postcss-loader',
  options: {
    plugins: [
      require('postcss-discard-duplicates'),
      require('postcss-import')(),
      require('postcss-url')(),
      require('postcss-cssnext')(),
      require('postcss-reporter')(),
      require('postcss-browser-reporter')({ disabled: isProd }),
    ]
  }
}

module.exports = {
  context: sourcePath,
  entry: {
    frontend: './app/index.tsx',
    vendor: [
      'react',
      'react-dom',
      'react-redux',
      'react-router',
      'redux'
    ]
  },
  output: {
    path: outPath,
    publicPath: '/',
    filename: isProd ? '[name].[chunkhash:8].cache.js' : '[name].js',
    chunkFilename: isProd ? '[name].[chunkhash:8].cache.js' : '[name].js',
    // includes comments in the generated code about where the code came from
    pathinfo: !isProd,
  },
  target: 'web',
  resolve: {
    extensions: ['.js', '.ts', '.tsx', '.css', '.less'],
    // Fix webpack's default behavior to not load packages with jsnext:main module
    // https://github.com/Microsoft/TypeScript/issues/11677
    mainFields: ['browser', 'main']
  },
  module: {
    rules: [
      {
        test: /\.ts$/,
        enforce: 'pre',
        loader: 'tslint-loader',
        options: {
          tsConfigFile: 'tsconfig.json',
          emitErrors: true,
          failOnHint: true
        }
      },
      // .ts, .tsx
      {
        test: /\.tsx?$/,
        use: isProd
          ? 'awesome-typescript-loader?module=es6'
          : [
            'react-hot-loader/webpack',
            'awesome-typescript-loader'
          ]
      },
      // css
      {
        test: /\.css$/,
        use: ExtractTextPlugin.extract({
          fallback: 'style-loader',
          use: [
            {
              loader: 'css-loader',
              query: {
                modules: true,
                sourceMap: !isProd,
                importLoaders: 1,
                localIdentName: '[local]__[hash:base64:5]'
              }
            },
            postCssLoader
          ]
        })
      },
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
                  require('postcss-discard-duplicates')
                ]
              }
            },
            postCssLoader,
            {
              loader: 'less-loader',
              options: {javascriptEnabled: true}
            }
          ]
        })
      }
    ],
  },
  plugins: _.compact([
    new Webpack.DefinePlugin({
      'process.env': {
        'NODE_ENV': JSON.stringify('production')
      }
    }),
    isProd ? new Webpack.HashedModuleIdsPlugin() : undefined,
    new Webpack.optimize.AggressiveMergingPlugin(),
    new ExtractTextPlugin({
      filename: '[name].[chunkhash:8].cache.css'
    }),
    new Webpack.NoEmitOnErrorsPlugin(),
    isProd
      ? undefined
      : new HtmlWebpackPlugin({
        template: 'index.html'
      }),
    // Ignore the very large locale files from moment (~1mb)
    // see: https://webpack.js.org/plugins/context-replacement-plugin/#usage
    new Webpack.ContextReplacementPlugin(/moment[\\/]locale$/, /^\.\/(en)$/),
    // // Enable to run the Webpack Bundle Analyzer
    // new BundleAnalyzerPlugin(),
    new ManifestPlugin()
  ]),
  /**
   * Webpack 4 optimization options.
   * Overwrite the default plugins/options here.
   * See: https://webpack.js.org/configuration/optimization/
   */
  optimization: {
    minimizer: [
      new UglifyJsPlugin({
        cache: true,
        sourceMap: true
      }),
      // // Optimize CSS, slows build considerably (+ ~30sec)
      // new OptimizeCSSAssetsPlugin({
      //   cssProcessor: require('cssnano'),
      //   cssProcessorOptions: {
      //     safe: true, discardComments: { removeAll: true }
      //   },
      //   canPrint: true
      // })
    ],
    splitChunks: {
      name: 'vendor',
      chunks(chunk) {
        return chunk.name !== 'frontend'
      }
    }
  },
  devServer: {
    contentBase: sourcePath,
    hot: true,
    stats: {
      warnings: false
    },
  },
  node: {
    // workaround for webpack-dev-server issue
    // https://github.com/webpack/webpack-dev-server/issues/60#issuecomment-103411179
    fs: 'empty',
    net: 'empty'
  },
  devtool: isProd ? 'source-map' : 'eval-source-map',
  performance: {
    hints: isProd ? false : 'warning'
  }
};
