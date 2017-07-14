var ReactComponent = require('./react_component');
var Prolog = require('proscript');
var Constants = require('./constants.js');

function handlerCallback(success)
{
    if (success == true)
    {
    }
    else if (success == false)
    {
        console.log("Event failed");
    }
    else
    {
        console.log("Event raised: " + success.toString());
    }

}

function keyDownHandler(event)
{
    if (this.keyDownHandler != null)
    {
        this.getOwnerDocument().triggerEvent(this.keyDownHandler,
                                             ReactComponent.serialize({key: Prolog._make_atom(event.key),
                                                                       keycode: Prolog._make_integer(event.keycode)}), handlerCallback);
    }
}

function Frame()
{
    ReactComponent.call(this);
    this.z = 0;
    this.contentPane = document.createElement("div");
    this.glassPane = document.createElement("div");
    this.glassPane.className = "proactive_frame_glasspane";
    this.contentPane.className = "proactive_frame_contentpane proactive_container " + this.getStyle();
    this.setDOMNode(document.createElement("div"));
    this.domNode.appendChild(this.glassPane);
    this.domNode.appendChild(this.contentPane);
    this.setZ();
    this.keyDownHandler = null;
    this.contentPane.onkeydown = keyDownHandler.bind(this);
    this.contentPane.tabIndex = -1;
}
Frame.prototype = new ReactComponent;

Frame.prototype.setZ = function()
{
    this.contentPane.style["z-index"] = this.z;
    this.glassPane.style["z-index"] = this.z-1;
}

Frame.prototype.appendChild = function(t)
{
    this.contentPane.appendChild(t.getDOMNode());
    t.setParent(this);
}

Frame.prototype.insertBefore = function(t, s)
{
    this.contentPane.insertBefore(t.getDOMNode(), s.getDOMNode());
    t.setParent(this);
}

Frame.prototype.replaceChild = function(n, o)
{
    this.contentPane.replaceChild(n.getDOMNode(), o.getDOMNode());
    n.setParent(this);
    o.setParent(null);
}

Frame.prototype.removeChild = function(t)
{
    this.contentPane.removeChild(t.getDOMNode());
    t.setParent(null);
}

Frame.prototype.setProperties = function(t)
{
    ReactComponent.prototype.setProperties.call(this, t);
    if (t.z_index !== undefined)
    {
        this.z = ReactComponent.numericValueOr(t.z_index, 0);
        this.setZ();
    }
    if (t.scroll !== undefined)
    {
        if (ReactComponent.isNull(t.scroll))
            this.baseClassName = "proactive_container";
        else if (Prolog._portray(t.scroll) == "both")
            this.baseClassName = "proactive_container scrollpane scroll";
        else if (Prolog._portray(t.scroll) == "horizontal")
            this.baseClassName = "proactive_container scrollpane scrollx";
        else if (Prolog._portray(t.scroll) == "vertical")
            this.baseClassName = "proactive_container scrollpane scrolly";
        this.restyle();
    }
    if (t.scroll !== undefined ||
        t.layout !== undefined ||
        t.fill !== undefined ||
        t["align-children"] !== undefined ||
        t["justify-content"] !== undefined)
    {
        this.contentPane.className = "proactive_frame_contentpane proactive_container " + this.getStyle();
    }
    if (t.onKeyDown !== undefined)
    {
        if (this.keyDownHandler != null)
            Prolog._free_local(this.keyDownHandler);
        if (ReactComponent.isNull(t.onKeyDown))
            this.keyDownHandler = null;
        else
            this.keyDownHandler = Prolog._make_local(t.onKeyDown);
    }
}

module.exports = Frame;
