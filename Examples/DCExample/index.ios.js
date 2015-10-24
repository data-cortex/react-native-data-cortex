/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 */
'use strict';

var React = require('react-native');
var {
  AppRegistry,
  StyleSheet,
  TouchableHighlight,
  Text,
  View,
} = React;

var DataCortex = require('react-native-data-cortex');
DataCortex.init("dYlBxjMTYkXadqhnOyHnjo7iGb5bW1y0", "rs_example");

var DCExample = React.createClass({

  componentDidMount: function() {
    DataCortex.event({"kingdom": "application_launch"});
    console.log('component mounted');
  },

  buttonOneClick: function() {
    console.log('buttonOneClick');
    DataCortex.event({
      "kingdom": "user_interaction",
      "phylum": "buttonOneClick",
    });
  },

  buttonTwoClick: function() {
    console.log('buttonTwoClick');
    DataCortex.economyEvent({
      "kingdom": "user_purchase",
      "phylum": "buttonTwoClick",
    }, "USD", 123.0);
  },

  render: function() {
    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>
          Welcome to React Native Data Cortex Example!
        </Text>
        <Text style={styles.instructions}>
          To send a generic event click the button bellow
        </Text>

        <TouchableHighlight onPress={this.buttonOneClick.bind(this)}>
          <Text style={styles.buttonOne}>
            Event!
          </Text>
        </TouchableHighlight>

        <Text style={styles.instructions}>
          To send an economy event 
          push the second button!
        </Text>
        <TouchableHighlight onPress={this.buttonTwoClick.bind(this)}>
          <Text style={styles.buttonOne}>
            EconomyEvent!
          </Text>
        </TouchableHighlight>
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
    padding: 30,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginTop: 15,
    marginBottom: 15,
  },
  buttonOne: {
    textAlign: 'center',
    padding: 10,
    backgroundColor: 'lightblue',
  },
});

AppRegistry.registerComponent('DCExample', () => DCExample);
