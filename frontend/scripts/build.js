/* eslint-disable no-console */
const webpack = require('webpack')
const createConfig = require('../webpack.config.js')

// stats options: https://webpack.js.org/configuration/stats/
// This set of options appears to generate the same output as the CLI with
// only the --display-error-details flag set.
const statsOptions = {
  colors: true,
  cached: false,
  chunks: false,
  // prevents some extract-text-webpack-plugin output
  children: false,
  modules: false,
  cachedAssets: false,
  exclude: ['node_modules', 'bower_components', 'components'],
  errorDetails: true
}


