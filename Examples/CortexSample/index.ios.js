/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 */
'use strict';

var React = require('react-native');
var {
  AppRegistry,
  StyleSheet,
  Text,
  View,
  requireNativeComponent,
} = React;

var DataCortex = require('react-native').NativeModules.DataCortex;

var CortexSample = React.createClass({
  componentWillMount() {
    DataCortex.sharedInstance("dYlBxjMTYkXadqhnOyHnjo7iGb5bW1y0", "rs_example");
  },
  render: function() {
      DataCortex.eventWithProperties({kingdome: "mychannel", "phylum": "login_screen"});
    DataCortex.economyWithProperties({kingdome: "mychannel", "phylum": "spend"}, "USD", 11.0);
    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>
          Welcome to React Native!
        </Text>
        <Text style={styles.instructions}>
          To get started, edit index.ios.js
        </Text>
        <Text style={styles.instructions}>
          Press Cmd+R to reload,{'\n'}
          Cmd+D or shake for dev menu
        </Text>
      </View>
    );
  }
});

var styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});

AppRegistry.registerComponent('CortexSample', () => CortexSample);
