"use strict";

var Prolog = require('../lib/proscript2/build/proscript.js');
var ReactComponent = require('./react_component');


function ComboItem()
{
    ReactComponent.call(this);
    this.setDOMNode(document.createElement("option"));
}

ComboItem.prototype = new ReactComponent;

ComboItem.prototype.setProperties = function(t)
{
   ReactComponent.prototype.setProperties.call(this, t);
    if (t.label !== undefined)
        this.domNode.textContent = t.label;
    if (t.value !== undefined)
        this.domNode.value = t.value;
}

module.exports = ComboItem;
