"use strict";

var Constants = require('./constants.js');
var PrologState = require('./prolog_state');
var ReactWidget = require('./react_widget');
var ProactiveComponentFactory = require('./proactive_component_factory');
var Prolog = require('../lib/proscript2/build/proscript.js');
var Errors = require('./errors.js');

var util = require('util');


function crossModuleCall(module, goal)
{
    return Prolog._make_compound(Constants.crossModuleCallFunctor, [Prolog._make_atom(module), goal]);
}

function isNull(t)
{
    return (Prolog._is_compound(t) && Prolog._term_functor(t) == Constants.curlyFunctor && Prolog._term_arg(t, 0) == Constants.nullAtom);
}

function addArgs(goal, glueArgs)
{
    if (Prolog._is_atom(goal))
        return Prolog._make_compound(goal, glueArgs);
    else if (Prolog._is_compound(goal))
    {
        var args = [];
        var functor = Prolog._term_functor(goal);
        var arity = Prolog._term_functor_arity(goal);
        for (var i = 0; i < arity; i++)
            args[i] = Prolog._term_arg(goal,i);
        return Prolog._make_compound(Prolog._term_functor_name(goal), args.concat(glueArgs));
    }
    return Errors.typeError(Constants.callableAtom, goal);
}

/* First, Prolog-type stuff */
module.exports["."] = function(state, key, value)
{

    if (isNull(state))
    {
        return Prolog._unify(value, Prolog._make_compound(Constants.curlyFunctor, [Constants.nullAtom]));
    }
    if (!Prolog._is_blob(state, "state"))
    {
        return Errors.typeError(Constants.prologStateAtom, state);
    }
    if (Prolog._is_atom(key))
    {
        //console.log("looking for: " + Prolog._atom_chars(key));
        //console.log(Prolog._get_blob("state", state).toString());
        //console.log(Prolog._format_term(null, 1200, Prolog._get_blob("state", state).get(key)));
        return Prolog._unify(value, Prolog._get_blob("state", state).get(key));
    }
    if (Prolog._is_compound(key))
    {
        var term = key;
        var functor = Prolog._term_functor(key);
        var glueArgs = [];
        var arity = Prolog._term_functor_arity(key);
        state = Prolog._get_blob("state", state);
        for (var i = 0; i < arity; i++)
            glueArgs[i] = Prolog._term_arg(key, i);
        var result = state.get(functor); // Really/
        if (isNull(result))
            return Prolog._unify(value, result);
        if (Prolog._is_compound(result))
        {
            if (Prolog._term_functor(result) == Constants.thisFunctor)
            {
                if (Prolog._is_compound(Prolog._term_arg(key, 1)) && Prolog._term_functor(Prolog._term_arg(key, 1)) == Constants.colonFunctor)
                {
                    var module = Prolog._term_arg(Prolog._term_arg(key, 1), 0);
                    var goal = Prolog._term_arg(Prolog._term_arg(key, 1), 1);
                    var newGoal = addArgs(goal, glueArgs);
                    return Prolog._unify(value, Prolog._make_compound(Prolog._term_functor(result), [_term_arg(term, 0), Prolog._make_compound(Constants.crossModuleCallFunctor, [module, newGoal])]));
                }
                else
                {
                    // No module
                    var newGoal = addArgs(Prolog._term_arg(term, 1), glueArgs);
                    return Prolog._unify(value, Prolog._make_compound(Prolog._term_functor(result), [_term_arg(term, 0), newGoal]));
                }
            }
            return Errors.typeError(Constants.gluableAtom, term);
        }
    }
    return Errors.typeError(Constants.prologStateKeyAtom, state);
}


module.exports["state_to_term"] = function(state, term)
{
    throw new Error("FIXME: state_to_term not implemented");
}

module.exports["on_server"] = function(goal)
{
    // This is quite complicated because we must mix all kinds of paradigms together :(
    throw new Error("Not migrated yet");
    // Later we must yield execution. Prepare the resume code
    var resume = Prolog._yield();
    var ws;
    if (this.foreign)
    {
        // We are backtracking. Try to get another solution by sending a ; and then yielding
        ws = this.foreign;
        ws.send(";");
        return 3; // YIELD
    }
    // First, create the websocket
    ws = new WebSocket(this.engine.goalURI);
    ws.onopen = function()
    {
        ws.send(Prolog.TermWriter.formatTerm({quoted:true}, 1200, goal) + ".\n");
        ws.send(";");
        // This is all we do for now. Either we will get an error, find out that the goal failed, or that it succeeded
    }
    ws.onmessage = function(event)
    {
        //console.log("Got a message: " + util.inspect(event.data));
        var term = Prolog.Parser.stringToTerm(event.data);
        if (term == Prolog.Constants.failAtom)
        {
            ws.close();
            resume(false);
        }
        else if (term == Constants.abortedAtom)
        {
            ws.close();
            resume(false);
        }
        else if (TAGOF(term) == CompoundTag)
        {
            if (Prolog._term_functor(term) == Constants.exceptionFunctor)
            {
                ws.close();
                resume(Prolog._term_arg(term, 0));
            }
            else if (Prolog._term_functor(term) == Constants.cutFunctor)
            {
                ws.close();
                resume(Prolog._unify(goal, Prolog._term_arg(term, 0)));
            }
            else
            {
                // OK, we need a backtrack point here so we can retry
                Prolog._create_choicepoint(ws, function() { ws.close(); });
                resume(Prolog._unify(goal, Prolog._term_arg(term, 0)));
            }
        }
    }.bind(this);
    ws.onerror = function(event)
    {
        console.log("WS error: " + event);
        ws.close();
        Errors.systemError(Prolog.AtomTerm.get(event.toString()));
        resume(2);
    }
    return 3; //  YIELD
}

module.exports["raise_event"] = function(a, b)
{
    // FLUX
    throw new Error("FIXME: raise_event not implemented");
}

module.exports["wait_for"] = function(fluxion)
{
    // FLUX
    throw new Error("FIXME: wait_for not implemented");
}

module.exports["get_store_state"] = function(fluxion, state)
{
    // FLUX
    throw new Error("FIXME: get_store_state not implemented");
}


module.exports["get_this"] = function(t)
{
    return Prolog._unify(t, this.proactive_context[this.proactive_context.length-1]);
}


module.exports["bubble_event"] = function(handler, event)
{
    if (Prolog._is_compound(handler) && Prolog._term_functor(handler) == Constants.thisFunctor)
    {
        var target = Prolog._get_blob("react_component", Prolog._term_arg(handler, 0));
        var resume = Prolog._yield();
        target.triggerEvent(Prolog._term_arg(handler, 1), event, resume);
        return YIELD;
    }
    // Otherwise it is just a goal - go ahead and call it with one extra arg
    var goal;
    if (Prolog._is_atom(handler))
        goal = Prolog._make_compound(handler, [event]);
    else if (Prolog._is_compound(handler))
    {
        var args = [];
        var functor = Prolog._term_functor(handler);
        var arity = Prolog._term_functor_arity(handler);
        for (var i = 0; i < arity; i++)
            args[i] = Prolog._term_arg(handler, i);
        goal = Prolog._make_compound(functor, args.concat([event]));
    }
    else
        Errors.typeError(Constants.callableAtom, goal);
    var savedState = Prolog._save_state();
    var resume = Prolog._yield();
    //var resumeAlways = function(){this.restoreState(savedState); resume(true);};
    Prolog._execute(goal, function(t) { _Prolog.restore_state(savedState); resume(t)}.bind(this));
    return YIELD;
}

/* And now the DOM glue */
module.exports["remove_child"] = function(parent, child)
{
    var p = Prolog._get_blob("react_component", parent);
    var c = Prolog._get_blob("react_component", child);
    var found = false;
    for (var i = 0; i < p.children.length; i++)
    {
        if (p.children[i] == c)
        {
            p.children.splice(i, 1);
            found = true;
            break;
        }
    }
    if (!found)
        throw new Error("Attempt to remove non-existent child");
    p.removeChild(c);
    return 1;
}

module.exports["append_child"] = function(parent, child)
{
    var p = Prolog._get_blob("react_component", parent);
    var c = Prolog._get_blob("react_component", child);
    p.children.push(c);
    p.appendChild(c);
    return 1;
}

module.exports["insert_before"] = function(parent, child, sibling)
{
    var p = Prolog._get_blob("react_component", parent);
    var c = Prolog._get_blob("react_component", child);
    var s = Prolog._get_blob("react_component", sibling);
    var found = false;
    for (var i = 0; i < p.children.length; i++)
    {
        if (p.children[i] == s)
        {
            p.children.splice(i, 0, c);
            found = true;
            break;
        }
    }
    if (!found)
        throw new Error("Attempt to insert before non-existent sibling");
    p.insertBefore(c, s);
    return 1;
}

module.exports["replace_child"] = function(parent, newChild, oldChild)
{
    var p = Prolog._get_blob("react_component", parent);
    var n = Prolog._get_blob("react_component", newChild);
    var o = Prolog._get_blob("react_component", oldChild);
    var found = false;
    for (var i = 0; i < p.children.length; i++)
    {
        if (p.children[i] == o)
        {
            p.children[i] = n;
            found = true;
            break;
        }
    }
    if (!found)
        throw new Error("Attempt to replace non-existent child");
    p.replaceChild(n, o);
    return 1;
}

module.exports["child_nodes"] = function(parent, children)
{
    var childNodes = Prolog._get_blob("react_component", parent).getChildren();
    var result = Constants.emptyListAtom;
    var i = childNodes.length;
    while(i--)
        result = Prolog._make_compound(Constants.listFunctor, [Prolog._make_blob("react_component", childNodes[i]), result]);
    var v = Prolog._unify(result, children);
    if (!v)
        console.log("Failed to unify children");
    return v;
}

module.exports["create_element"] = function(context, tagname, domnode)
{
    var node = ProactiveComponentFactory.createElement(Prolog._atom_chars(tagname), Prolog._get_blob("react_component", context));
    node.setOwnerDocument(Prolog._get_blob("react_component", context));
    return Prolog._unify(domnode, Prolog._make_blob("react_component", node));
}

module.exports["create_text_node"] = function(context, text, domnode)
{
    var node = ProactiveComponentFactory.createElement('Broken', Prolog._get_blob("react_component", context));
    node.setOwnerDocument(Prolog._get_blob("react_component", context));
    return Prolog._unify(domnode, Prolog._make_blob("react_component", node));
}

module.exports["parent_node"] = function(node, parent)
{
    return _Prolog.unify(parent, Prolog._make_blob("react_component", Prolog._get_blob("react_component", node).getParent()));
}

module.exports["node_type"] = function(node, type)
{
    return Prolog._unify(type, Constants.nodeAtom);
}

module.exports["set_vdom_properties"] = function(domNode, list)
{
    if (list == Constants.emptyListAtom)
        return 1;
    var l = list;
    var properties = {};
    while (Prolog._is_compound(l) && Prolog._term_functor(l) == Constants.listFunctor)
    {
        var head = Prolog._term_arg(l, 0);
        l = Prolog._term_arg(l, 1);
        if (Prolog._is_compound(head) && Prolog._term_functor(head) == Constants.equalsFunctor)
        {
            var name = Prolog._term_arg(head, 0);
            var value = Prolog._term_arg(head, 1);
            if (!Prolog._is_atom(name))
                Errors.typeError(Constants.atomAtom, name);
            properties[Prolog._atom_chars(name)] = value;
        }
        else
        {
            return Errors.typeError(Constants.attributeAtom, head);
        }
    }
    if (l != Constants.emptyListAtom)
        return Errors.typeError(Constants.listAtom, list);
    Prolog._get_blob("react_component", domNode).setProperties(properties);
    return true;
}

module.exports["replace_node_data"] = function(domNode, properties)
{
    throw new Error("FIXME: replace_node_data not implemented");
}

module.exports["destroy_widget"] = function(domNode)
{
    throw new Error("FIXME: destroy_widget not implemented");
}

module.exports["init_widget"] = function(context, properties, domNode)
{
    if (!Prolog._is_blob(context, "react_component"))
        return Errors.typeError(Constants.blobAtom, context);
    var parentContext = Prolog._get_blob("react_component", context);
    var resume = Prolog._yield();
    var widget = new ReactWidget(parentContext,
                                 parentContext.getEngine(),
                                 Prolog._atom_chars(Prolog._term_arg(properties, 0)),
                                 PrologState.fromList(Prolog._term_arg(properties, 1)),
                                 function(widget)
                                 {
                                     resume(Prolog._unify(domNode, Prolog._make_blob("react_component", widget)));
                                 }.bind(this));
    return 3; // YIELD
}

module.exports["update_widget"] = function(newVDom, oldVDom, widget, newDomNode)
{
    var newProperties = PrologState.fromList(Prolog._term_arg(newVDom, 1));
    newProperties.map.children = Prolog._term_arg(newVDom, 2);
    var resume = Prolog._yield();
    Prolog._get_blob("react_component", widget).updateWidget(newProperties, function(newWidget)
                                                           {
                                                               if (newWidget === Prolog._get_blob("react_component", widget))
                                                               {
                                                                   resume(Prolog._unify(newDomNode, widget));
                                                               }
                                                               else
                                                               {
                                                                   resume(Prolog._unify(newDomNode, Prolog._make_blob("react_component", newWidget)))
                                                               };
                                                           }.bind(this));

    return 3; // YIELD;
}

