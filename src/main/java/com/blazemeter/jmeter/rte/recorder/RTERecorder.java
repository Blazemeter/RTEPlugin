package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.recorder.gui.RTERecorderGui;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.assertions.gui.AssertionGui;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.control.TransactionController;
import org.apache.jmeter.control.gui.TransactionControllerGui;
import org.apache.jmeter.engine.util.ValueReplacer;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.timers.Timer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.Visualizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RTERecorder extends GenericController implements ThreadListener {

  private static final Logger LOG = LoggerFactory.getLogger(RTERecorder.class);
  private static final String LOGIC_CONTROLLER_GUI = RTERecorderGui.class.getName();
  private static final String TRANSACTION_CONTROLLER_GUI = TransactionControllerGui.class.getName();
  private static final String ASSERTION_GUI = AssertionGui.class.getName();
  private static ThreadLocal<Map<String, RteProtocolClient>> connections = ThreadLocal
      .withInitial(HashMap::new);
  private JMeterTreeModel nonGuiTreeModel;
  private Set<Class<?>> addableInterfaces = new HashSet(Arrays.asList(
      Visualizer.class, ConfigElement.class,
      Assertion.class, Timer.class, PreProcessor.class, PostProcessor.class));
  private volatile int groupingMode = 0;
  private volatile boolean addAssertions = false;
  private long lastTime = 0L;
  private long sampleGap;
  
  @Override
  public void threadStarted() {

  }

  @Override
  public void threadFinished() {
    closeConnections();
  }

  private void closeConnections() {
    connections.get().values().forEach(c -> {
      try {
        c.disconnect();
      } catch (Exception e) {
        LOG.error("Problem while closing RTE connection", e);
      }
    });
    connections.get().clear();
  }

  private void placeSampler(HTTPSamplerBase sampler,
                            TestElement[] testElements, JMeterTreeNode myTarget) {
    try {
      JMeterTreeModel treeModel = this.getJmeterTreeModel();
      boolean firstInBatch = false;
      long now = System.currentTimeMillis();
      long deltaT = now - this.lastTime;
      int cachedGroupingMode = this.groupingMode;
      if (deltaT > this.sampleGap) {
        if (!myTarget.isLeaf() && cachedGroupingMode == 1) {
          this.addDivider(treeModel, myTarget);
        }

        if (cachedGroupingMode == 2) {
          this.addSimpleController(treeModel, myTarget, sampler.getName());
        }

        if (cachedGroupingMode == 4) {
          this.addTransactionController(treeModel, myTarget, sampler.getName());
        }

        firstInBatch = true;
      }

      if (this.lastTime == 0L) {
        deltaT = 0L;
      }

      this.lastTime = now;
      if (cachedGroupingMode == 3) {
        if (!firstInBatch) {
          return;
        }

        sampler.setFollowRedirects(true);
        sampler.setImageParser(true);
      }

      if (cachedGroupingMode == 2 || cachedGroupingMode == 4) {
        for (int i = myTarget.getChildCount() - 1; i >= 0; --i) {
          JMeterTreeNode c = (JMeterTreeNode) myTarget.getChildAt(i);
          if (c.getTestElement() instanceof GenericController) {
            myTarget = c;
            break;
          }
        }
      }

      JMeterTreeNode finalMyTarget = myTarget;
      boolean finalFirstInBatch = firstInBatch;
      long finalDeltaT = deltaT;
      JMeterUtils.runSafe(true, () -> {
        try {
          JMeterTreeNode newNode = treeModel.addComponent(sampler, finalMyTarget);
          if (finalFirstInBatch) {
            if (this.addAssertions) {
              this.addAssertion(treeModel, newNode);
            }

            this.addTimers(treeModel, newNode, finalDeltaT);
          }

          if (testElements != null) {
            int var10 = testElements.length;

            for (TestElement testElement : testElements) {
              if (this.isAddableTestElement(testElement)) {
                treeModel.addComponent(testElement, newNode);
              }
            }
          }
        } catch (IllegalUserActionException illegalUserAction) {
          LOG.error("Error placing sampler", illegalUserAction);
          JMeterUtils.reportErrorToUser(illegalUserAction.getMessage());
        }

      });
    } catch (Exception exception) {
      LOG.error("Error placing sampler", exception);
      JMeterUtils.reportErrorToUser(exception.getMessage());
    }

  }

  private void addDivider(JMeterTreeModel model, JMeterTreeNode node) {
    GenericController sc = new GenericController();
    sc.setProperty("TestElement.gui_class", LOGIC_CONTROLLER_GUI);
    sc.setName("-------------------");
    this.safelyAddComponent(model, node, sc);
  }

  private JMeterTreeModel getJmeterTreeModel() {
    return this.nonGuiTreeModel == null ? GuiPackage.getInstance()
        .getTreeModel() : this.nonGuiTreeModel;
  }

  private void safelyAddComponent(JMeterTreeModel model,
                                  JMeterTreeNode node, GenericController controller) {
    JMeterUtils.runSafe(true, () -> {
      try {
        model.addComponent(controller, node);
      } catch (IllegalUserActionException actionException) {
        LOG.error("Program error", actionException);
        throw new Error(actionException);
      }
    });
  }
  
  private void addSimpleController(JMeterTreeModel model, JMeterTreeNode node, String name) {
    GenericController sc = new GenericController();
    sc.setProperty("TestElement.gui_class", LOGIC_CONTROLLER_GUI);
    sc.setName(name);
    this.safelyAddComponent(model, node, sc);
  }

  private void addTransactionController(JMeterTreeModel model, JMeterTreeNode node, String name) {
    TransactionController sc = new TransactionController();
    sc.setIncludeTimers(false);
    sc.setProperty("TestElement.gui_class", TRANSACTION_CONTROLLER_GUI);
    sc.setName(name);
    this.safelyAddComponent(model, node, sc);
  }

  private void addAssertion(JMeterTreeModel model, JMeterTreeNode node)
      throws IllegalUserActionException {
    ResponseAssertion ra = new ResponseAssertion();
    ra.setProperty("TestElement.gui_class", ASSERTION_GUI);
    ra.setName(JMeterUtils.getResString("assertion_title"));
    ra.setTestFieldResponseData();
    model.addComponent(ra, node);
  }

  private void addTimers(JMeterTreeModel model, JMeterTreeNode node, long deltaT) {
    TestPlan variables = new TestPlan();
    variables.addParameter("T", Long.toString(deltaT));
    ValueReplacer replacer = new ValueReplacer(variables);
    JMeterTreeNode mySelf = model.getNodeOf(this);
    if (mySelf != null) {
      Enumeration children = mySelf.children();

      while (children.hasMoreElements()) {
        JMeterTreeNode templateNode = (JMeterTreeNode) children.nextElement();
        if (templateNode.isEnabled()) {
          TestElement template = templateNode.getTestElement();
          if (template instanceof Timer) {
            TestElement timer = (TestElement) template.clone();

            try {
              timer.setComment("Recorded:" + deltaT + "ms");
              replacer.undoReverseReplace(timer);
              model.addComponent(timer, node);
            } catch (IllegalUserActionException | InvalidVariableException 
                invalidVariableException) {
              LOG.error("Program error adding timers", invalidVariableException);
              throw new Error(invalidVariableException);
            }
          }
        }
      }
    }

  }

  private boolean isAddableTestElement(TestElement testElement) {
    if (this.hasCorrectInterface(testElement, this.addableInterfaces)) {
      if (testElement.getProperty("TestElement.gui_class") != null) {
        return true;
      } else {
        LOG.error("Cannot add element that lacks the TestElement.gui_class property" +
            "  as testElement:" + testElement);
        return false;
      }
    } else {
      return false;
    }
  }

  private boolean hasCorrectInterface(Object obj, Set<Class<?>> klasses) {
    Iterator var3 = klasses.iterator();

    Class klass;
    do {
      if (!var3.hasNext()) {
        return false;
      }

      klass = (Class) var3.next();
    } while (klass == null || !klass.isInstance(obj));

    return true;
  }
}
