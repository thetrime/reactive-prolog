"use strict";

window.onPrologReady = function(Prolog)
{
    var ProactiveComponentFactory = require('./proactive_component_factory.js');
    var Constants = require('./constants');
    var PrologEngine = require('./prolog_engine')
    var ReactWidget = require('./react_widget');
    var PrologState = require('./prolog_state');
    var ReactComponent = require('./react_component.js');
    if (onProactiveReady !== undefined)
	onProactiveReady({render: function(url, rootElementId, container, callback)
			  {
			      var engine = new PrologEngine(url, rootElementId, function()
							      {
								  new ReactWidget(null, engine, rootElementId, PrologState.emptyState, function(widget)
										    {
											container.className += " proactive_container vertical_layout vertical_fill horizontal_fill";
											container.appendChild(widget.getDOMNode());
											callback();
										    })
							      });
                          },
                          registerPredicate: Prolog._define_foreign,
			  registerComponent: ProactiveComponentFactory.registerComponent,
			  ReactComponent: ReactComponent,
			  Constants: Constants,
                          make_atom: Prolog._make_atom,
                          atom_chars: Prolog._atom_chars}
			);
}
console.log("Waiting for Prolog...");
if (typeof window.proactivePrefixURL !== 'undefined')
    window.proscriptPrefixURL = window.proactivePrefixURL;
else
    window.proscriptPrefixURL = "/";
require('../lib/proscript2/build/proscript.js');


module.exports = {};
