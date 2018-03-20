import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * Created by yair.pecherer on 04/01/2018.
 * Search the accessibilityNodeInfo tree non-recursively
 */

class AccessibilitySearchUtils {
    static AccessibilitySearchUtils instance = null;

    private List<AccessibilityNodeInfo> mResults = new ArrayList<>();
    private Stack<AccessibilityNodeInfo> mHistory = new Stack<>();
    private Stack<Integer> mIndexStack = new Stack<>();
    private Set<AccessibilityNodeInfo> mAddedItems = new HashSet<>();

    static void init() {
        if(instance == null) {
            instance = new AccessibilitySearchUtils();
        }

        instance.mResults.clear();
        instance.mHistory.clear();
        instance.mIndexStack.clear();
        instance.mAddedItems.clear();
    }

    static List<AccessibilityNodeInfo> buildAccessibilityList(AccessibilityNodeInfo rootNode) {
        init();

        instance.mResults.add(rootNode);

        // Traverse until last left leaf
        int index = 0;
        AccessibilityNodeInfo parent = rootNode;
        AccessibilityNodeInfo child = null;
        boolean canContinue = true;
        while(parent != null && (child = parent.getChild(index)) != null) {
            if(!instance.mAddedItems.contains(child)) {
                instance.mResults.add(child);
                instance.mAddedItems.add(child);
            }

            if(instance.mHistory.size() == 0 || parent != instance.mHistory.peek()) {
                instance.mHistory.push(parent);
            }

            // Has children
            if(child.getChildCount() > 0 && canContinue) {
                parent = child;
                instance.mIndexStack.push(index);
                index = 0;
            }
            else {
                // Does this parent have more children?
                if(index < parent.getChildCount() - 1) {
                    index++;
                    canContinue = true;
                }
                else {
                    child = instance.mHistory.pop();
                    parent = instance.mHistory.size() > 0 ? instance.mHistory.pop() : null;
                    index = instance.mIndexStack.size() > 0 ? instance.mIndexStack.pop() : 0;
                    canContinue = false;
                }
            }
        }

        return instance.mResults;
    }

    static void searchTree(AccessibilityNodeInfo rootNode, IAccessibilityTreeSearchAssistant searchAssistant, String textToSearch) {
        if(rootNode == null) {
            return;
        }

        List<AccessibilityNodeInfo> list = buildAccessibilityList(rootNode);


        for(AccessibilityNodeInfo node : list) {
            if(searchAssistant.search(node, textToSearch)) {
                searchAssistant.onNodeFound(node);
            }
        }
    }

    static <T> List<T> getIntersection(List<T>... lists) {
        if(lists == null) {
            return null;
        }

        List<T> nodes = null;

        for(List<T> list : lists) {
            if(list != null) {
                if(nodes != null) {
                    nodes.retainAll(list);
                }
                else {
                    nodes = list;
                }
            }
        }

        return nodes;
    }

    interface IAccessibilityTreeSearchAssistant {
        boolean search(AccessibilityNodeInfo child, String textToSearch);
        void onNodeFound(AccessibilityNodeInfo nodeFound);
    }
}
