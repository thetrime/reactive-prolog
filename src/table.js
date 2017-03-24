var ReactComponent = require('./react_component');

function Table()
{
    ReactComponent.call(this);
    this.table = document.createElement("table");
    // table does not play nice with flexbox. Instead put it inside a div which will and set it to 100% width
    this.table.style.width = "100%";
    this.table.className = "react_table";
    this.table.style["border-spacing"] = 0;
    var node = document.createElement("div");
    node.addEventListener("scroll", function()
                          {
                              var translate = "translate(0,"+this.domNode.scrollTop+"px)";
                              console.log(translate);
                              if (this.table.tHead !== undefined)
                              {
                                  this.table.tHead.style.transform = translate;
                                  console.log(this.table.tHead);
                                  console.log(this.table.tHead.style);
                                  console.log(this.table.tHead.style.transform);
                              }
                          }.bind(this));
    this.baseClassName = "table_container"
    node.appendChild(this.table);
    this.setDOMNode(node);
}
Table.prototype = new ReactComponent;

Table.prototype.getStyle = function()
{
    return "table_container";
}

Table.prototype.appendChild = function(t)
{
    this.table.appendChild(t.getDOMNode());
    t.setParent(this);
}

Table.prototype.insertBefore = function(t, s)
{
    this.table.insertBefore(t.getDOMNode(), s.getDOMNode());
    t.setParent(this);
}

Table.prototype.replaceChild = function(n, o)
{
    this.table.replaceChild(n.getDOMNode(), o.getDOMNode());
    n.setParent(this);
    o.setParent(null);
}


Table.prototype.removeChild = function(t)
{
    this.table.removeChild(t.getDOMNode());
    t.setParent(null);
}


module.exports = Table;
